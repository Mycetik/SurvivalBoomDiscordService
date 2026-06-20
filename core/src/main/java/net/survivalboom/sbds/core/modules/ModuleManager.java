package net.survivalboom.sbds.core.modules;

import net.survivalboom.sbds.api.libraries.ILibrary;
import net.survivalboom.sbds.api.libraries.LibraryDeclaration;
import net.survivalboom.sbds.api.libraries.LibrarySatisfyConfiguration;
import net.survivalboom.sbds.api.modules.*;
import net.survivalboom.sbds.api.modules.dependencies.ModuleDependency;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.libraries.DynamicClassLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ModuleManager extends Manager implements IModuleManager {

    private static final Logger log = LoggerFactory.getLogger("ModuleManager");

    private final SBDS sbds;

    private final File modulesDir;

    private final Map<String, Module> modules = new HashMap<>();

    private final ModulesClasspath modulesClasspath;


    public ModuleManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.modulesClasspath = new ModulesClasspath(this);
        this.modulesDir = new File(sbds.getWorkingDir(), "modules");
    }

    //
    // MANAGER
    //

    @Override
    protected void init0() {

        modulesClasspath.init();

        log.info("Loading modules...");
        //noinspection ResultOfMethodCallIgnored
        modulesDir.mkdirs();

        //noinspection DataFlowIssue
        List<File> modulesFiles = Arrays.stream(modulesDir.listFiles())
                .filter(file -> file.getName().endsWith(".jar"))
                .toList();

        if (modulesFiles.isEmpty()) {
            log.info("No modules found! Skipping...");
            return;
        }

        List<ModuleMetaLoadResult> modulesMetas = new ArrayList<>();
        for (File file : modulesFiles) {

            ModuleMetaLoadResult result;
            try {
                result = loadModuleMeta(file);
            }

            catch (ModuleMeta.InvalidMetaException e) {
                log.error("Invalid module file `{}`. Skipping...", file.getName(), e);
                continue;
            }

            modulesMetas.add(result);

        }

        if (!modulesMetas.isEmpty()) {

            log.info("Found {} valid modules. Loading them... \n - {}",
                    modulesMetas.size(),
                    String.join(", ", modulesMetas.stream()
                            .map(meta -> meta.meta().getName())
                            .toList())
            );

        }

        for (ModuleMetaLoadResult meta : modulesMetas) {

            String name = meta.meta().getName();
            log.info("Loading module {}...", name);

            try {
                createModule(meta.meta(), meta.file(), null);
            }

            catch (ModuleUnsatisfiedLibraryException e) {
                log.error("Failed to download library `{}`. Refusing to load.", e.getLibrary(), e.getCause());
            }

            catch (ModuleUnsatisfiedDependencyException e) {
                log.error("Module `{}` requires `{}` as a dependency. No module with that id was found. Refusing to load.", name, e.getDependency().id());
            }

            catch (ModuleLoadingException e) {
                log.error("Module `{}` failed to load!", name, e);
            }

            catch (ModuleRefusedException e) {
                log.error("Module `{}` refused to load! Maybe it's mad on you?", name, e);
            }

        }

        for (Module module : modules.values()) {

            try {
                enableModule(module);
            }

            catch (ModuleStateCallbackException e) {
                log.error("An exception was thrown when attempted to enable module `{}`.", module, e);
            }

            catch (ModuleRefusedException e) {
                log.error("Module `{}` refused to start! Maybe it's mad on you?", module.getName(), e);
            }

        }

    }

    @Override
    protected void shutdown0() {

        if (!modules.isEmpty()) {
            log.info("Disabling modules...");
        }

        var modules = getModules();
        for (IModule module : modules) {

            if (module.isEnabled()) {

                try {
                    disableModule(module);
                }

                catch (ModuleStateCallbackException e) {
                    log.error("An exception was thrown in {}.onDisable().", module.getName(), e);
                }

            }

            try {
                unloadModule(module);
            }

            catch (ModuleDependantException e) {
                log.error("Catastrophic failure!", e);
            }

            catch (ModuleStateCallbackException e) {
                log.error("An exception was thrown in {}.onUnload().", module.getName(), e);
            }

        }

        modulesClasspath.shutdown();

    }

    //
    // MODULES
    //

    // LOAD/UNLOAD //

    @Override
    public synchronized @NotNull Module loadModule(@NotNull File file) throws ModuleLoadingException, ModuleRefusedException, ModuleUnsatisfiedDependencyException, ModuleUnsatisfiedLibraryException {

        // Завантажуємо ModuleMeta із .jar файлу //

        ModuleMetaLoadResult result;
        try {
            result = loadModuleMeta(file);
        }

        catch (ModuleMeta.InvalidMetaException e) {
            throw new ModuleLoadingException(e);
        }

        // Створюємо модуль //

        return createModule(result.meta(), result.file(), null);

    }

    @SuppressWarnings("unchecked")
    public synchronized @NotNull Module createModule(@NotNull ModuleMeta meta, @Nullable ModuleFile file, @Nullable File dataDir) throws ModuleLoadingException, ModuleRefusedException, ModuleUnsatisfiedDependencyException, ModuleUnsatisfiedLibraryException {

        Objects.requireNonNull(meta, "meta == null");
        checkValid();

        // Перевіряємо чи модуль з такою назвою вже існує //
        if (modules.containsKey(meta.getId())) {
            throw new ModuleLoadingException("Module with name `" + meta.getId() + "` already loaded");
        }

        // Перевіряємо на правильність теку із даними модуля //

        if (dataDir == null) {
            dataDir = new File(modulesDir, meta.getName());
        }

        if (dataDir.isFile()) {
            throw new ModuleLoadingException("Module data directory `" + dataDir.getPath() + "` is not a directory");
        }

        // Перевіряємо залежності модуля //

        for (ModuleDependency dependency : meta.getDependencies()) {

            String dpId = dependency.id();
            boolean required = dependency.required();

            if (required && !modules.containsKey(dpId)) {
                throw new ModuleUnsatisfiedDependencyException(dependency, "Dependency `" + dpId + "` is not present! Load the dependency first.");
            }

        }

        // Завантажуємо бібліотеки модуля //

        LibrarySatisfyConfiguration libraries = meta.getLibraries();
        List<ILibrary> loadedLibraries = null;
        if (libraries != null && !libraries.isEmpty()) {

            var result = sbds.getLibrariesManager().satisfy(libraries);

            for (var entry : result.failed().entrySet()) {

                LibraryDeclaration library = entry.getKey();
                Exception cause = entry.getValue();

                throw new ModuleUnsatisfiedLibraryException(library, "Failed to download module library `" + library + "`", cause);
            }

            loadedLibraries = new ArrayList<>();
            loadedLibraries.addAll(result.downloaded());
            loadedLibraries.addAll(result.skipped());

        }

        // Створюємо модуль //

        Module module = new Module(meta, file, dataDir, loadedLibraries, this);

        DynamicClassLoader classLoader = module.getClassLoader();
        classLoader.addClassSupplier("MODULES-CLASSPATH", cl -> modulesClasspath.request(cl, module));

        if (file != null) {
            classLoader.addSource(file.file());
        }

        modules.put(meta.getId(), module);
        modulesClasspath.purgeCache();

        // Шукаємо головний клас модуля.
        String mainClassName = meta.getMain();
        Class<? extends ModuleMain> clazz = (Class<? extends ModuleMain>) module.getClassLoader().getClass(mainClassName, false, false);
        if (clazz == null) {
            modules.remove(meta.getId());
            throw new ModuleLoadingException("Module main class `" + mainClassName + "` not found in module ClassLoader");
        }

        // Шукаємо конструктор за яким ми зможемо створити об'єкт.
        Constructor<? extends ModuleMain> constructor;
        try {
            constructor = clazz.getDeclaredConstructor();
        }

        catch (NoSuchMethodException e) {
            modules.remove(meta.getId());
            throw new ModuleLoadingException("No public no-args constructor found in class `" + mainClassName + "`");
        }

        // Створюємо об'єкт головного класа модуля.
        ModuleMain main;
        try {
            main = constructor.newInstance();
        }

        catch (Throwable e) {
            modules.remove(meta.getId());
            throw new ModuleLoadingException("Failed to create instance of module main class", e);
        }

        module.moduleMain = main;
        main.init(module, sbds);

        // Після створення об'єкта головного класу модуля, виконуємо onLoad()
        try {
            main.onLoad();
        }

        catch (ModuleRefusedException e) {
            modules.remove(meta.getId());
            throw e;
        }

        catch (Throwable e) {
            modules.remove(meta.getId());
            throw new ModuleLoadingException("An exception occurred in onLoad()", e);
        }

        return module;

    }

    @Override
    public @NotNull ModuleMetaLoadResult loadModuleMeta(@NotNull File file) throws ModuleMeta.InvalidMetaException {

        checkValid();

        // Перевіряємо файл на правильність //

        Objects.requireNonNull(file, "file == null");

        if (!file.exists()) {
            throw new IllegalArgumentException("File `" + file.getPath() + "` does not exist");
        }

        if (!file.isFile()) {
            throw new IllegalArgumentException("File `" + file.getPath() + "` is not a file");
        }

        if (!file.getName().endsWith(".jar")) {
            throw new IllegalArgumentException("File `" + file.getPath() + "` is not a jar file");
        }

        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Invalid file");
        }

        // Намагаємось відкрити jar файл та прочитати його вміст //

        JarFile jarFile;
        try {
            jarFile = new JarFile(file);
        }

        catch (IOException e) {
            throw new ModuleMeta.InvalidMetaException(e);
        }

        ZipEntry entry = jarFile.getEntry("module.yml");
        if (entry == null) {
            throw new ModuleMeta.InvalidMetaException("Module meta file `module.yml` not found in target jar file `" + file.getPath() + "`");
        }

        ModuleMeta meta;
        try (InputStream stream = jarFile.getInputStream(entry)) {
            meta = ModuleMeta.fromStream(stream);
        }

        catch (IOException e) {
            throw new ModuleMeta.InvalidMetaException("Failed to load module meta from `module.yml`", e);
        }

        return new ModuleMetaLoadResult(meta, new ModuleFile(file, jarFile));

    }

    @Override
    public synchronized void unloadModule(@NotNull IModule module) throws ModuleStateCallbackException, ModuleDependantException {

        checkModuleValid(module);

        if (module.isEnabled()) {
            throw new IllegalStateException("Module must be disabled first in order to unload");
        }

        IModule dependent = modules.values().stream()
                .filter(m -> m.getMeta().getDependencies().stream().anyMatch(dep -> dep.id().equals(module.getName())))
                .findAny()
                .orElse(null);

        if (dependent != null) {
            throw new ModuleDependantException(dependent, "Could not unload module. Found module `" + dependent.getName() + "` depends on this module. You must unload it first in order to unload this module.");
        }

        try {
            module.getMain().onUnload();
        }

        catch (Throwable e) {
            throw new ModuleStateCallbackException(e);
        }

        finally {
            this.modules.remove(module.getMeta().getId());
            modulesClasspath.purgeCache();
        }

    }

    // ENABLE/DISABLE //

    @Override
    public synchronized void enableModule(@NotNull IModule imodule) throws ModuleStateCallbackException, ModuleRefusedException {

        checkModuleValid(imodule);

        if (imodule.isEnabled()) {
            throw new IllegalStateException("Module is not disabled");
        }

        log.info("Enabling {}...", imodule.getName());

        Module module = (Module) imodule;
        module.enabled = true;

        try {
            imodule.getMain().onEnable();
        }

        catch (ModuleRefusedException e) {
            module.enabled = false;
            throw e;
        }

        catch (Throwable e) {
            module.enabled = false;
            sbds.getRegistrationRegistry().removeModuleRegistrations(imodule);
            throw new ModuleStateCallbackException(e);
        }

    }

    @Override
    public synchronized void disableModule(@NotNull IModule imodule) throws ModuleStateCallbackException {

        checkModuleValid(imodule);

        if (!imodule.isEnabled()) {
            throw new IllegalStateException("Module is not enabled");
        }

        Module module = (Module) imodule;

        log.info("Disabling {}...", module.getName());

        try {
            imodule.getMain().onDisable();
        }

        catch (Throwable e) {
            throw new ModuleStateCallbackException(e);
        }

        finally {
            module.enabled = false;
            sbds.getRegistrationRegistry().removeModuleRegistrations(imodule);
        }

    }

    //
    // GETTERS
    //

    @Override
    public @Nullable IModule getModule(@NotNull String name) {
        return modules.get(name);
    }


    //
    // GETTERS
    //

    public @NotNull SBDS getSbds() {
        return sbds;
    }

    public @NotNull List<IModule> getModules() {
        checkValid();
        return new ArrayList<>(getModules0());
    }

    @Override
    public @NotNull File getModulesDir() {
        return modulesDir;
    }

    public @NotNull List<Module> getModules0() {
        checkValid();
        return new ArrayList<>(modules.values());
    }

    public @NotNull ModulesClasspath getModulesClassesSharingManager() {
        checkValid();
        return modulesClasspath;
    }

    //
    // INTERNAL
    //

    @Override
    public @NotNull IModule checkModuleValid(@NotNull IModule module) {

        Objects.requireNonNull(module, "module == null");
        checkValid();

        if (!(module instanceof Module m)) {
            throw new IllegalArgumentException("IModule object is instance of `" + module.getClass().getName() + "` not a net.survivalboom.sbds.core.modules.Module object! Are you trying to break SBDS?");
        }

        if (!modules.containsValue(m)) {
            throw new IllegalArgumentException("Module object is not registered in the ModuleManager. Is module unloaded?");
        }

        if (!m.isValid()) {
            throw new IllegalArgumentException("Module object is registered in ModuleManager, but Method#valid returned false. Did you break something?");
        }

        return m;

    }

    @Override
    public @NotNull IModule checkModuleEnabled(@NotNull IModule imodule, @Nullable String message) {

        Module module = (Module) checkModuleValid(imodule);
        if (!module.isEnabled()) {
            String msg = message != null ? message : "Module must be enabled";
            throw new IllegalArgumentException(msg);
        }

        return module;

    }

}
