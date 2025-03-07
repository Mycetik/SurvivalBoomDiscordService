package net.survivalboom.sbds.core.modules;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.IModuleManager;
import net.survivalboom.sbds.api.modules.InvalidModuleException;
import net.survivalboom.sbds.api.modules.InvalidModuleMetaException;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.api.utils.Manager;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ModuleManager extends Manager implements IModuleManager {

    private final SBDS sbds;

    private final Logger logger;

    private final File modulesDir;

    private final Map<String, Module> modules = new HashMap<>();

    private final ModulesClasspath modulesClasspath = new ModulesClasspath();

    public ModuleManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.modulesDir = new File(sbds.getWorkingDir(), "modules");
        this.logger = LoggerFactory.getLogger("ModuleManager");
    }

    @Override
    protected void init0() {

        sbds.getLibrariesManager().getJarLoader().configure(modulesClasspath::findInModules);

        logger.info("Loading modules...");
        modulesDir.mkdirs();

        List<File> modulesFiles = searchForModulesFiles();
        if (modulesFiles.isEmpty()) {
            logger.info("No modules found! Skipping...");
            return;
        }

        List<Module> modules = prepareModules(modulesFiles);
        logger.info("Found {} modules to load! \n - {}", modules.size(), String.join(", ", modules.stream().map(loader -> String.format("%s v%s", loader.getMeta().getName(), loader.getMeta().getVersion())).toList()));


        for (Module module : modules) {
            loadModule(module);
        }

        for (Module module : getModules0()) {
            enableModule(module);
        }

    }

    @Override
    protected void shutdown0() {

        logger.info("Disabling modules...");

        List<Module> modules = getModules0();
        for (Module module : modules) {
            disableModule(module);
        }

        for (Module module : modules) {
            unloadModule(module);
        }

    }

    //
    // MODULES
    //

    /* LOAD/UNLOAD */

    @Override
    public @Nullable Module loadModule(@NotNull File file) {

        try {
            return loadModule0(file);
        }

        catch (Throwable t) {
            logger.error("Failed to load {}", file.getName(), t);
            return null;
        }

    }

    public @Nullable Module loadModule(@NotNull Module module) {

        try {
            return loadModule1(module);
        }

        catch (Throwable t) {
            logger.error("Failed to load {}", module.getMeta().getName(), t);
            return null;
        }

    }

    private @NotNull Module loadModule0(@NotNull File file) throws IOException, InvalidModuleMetaException, InvalidModuleException {

        checkValid();

        if (!file.exists() || !file.isFile()) throw new IllegalArgumentException("Invalid file");

        Module module = new Module(this, file);
        module.readMeta();

        if (!IModuleManager.checkNameValid(module.getMeta().getName())) throw new InvalidModuleMetaException("Module name contains illegal characters. Allowed characters: " + String.join(" ", IModuleManager.ALLOWED_CHARACTERS));

        return loadModule1(module);

    }

    private synchronized @NotNull Module loadModule1(@NotNull Module module) throws InvalidModuleException {

        checkValid();

        ModuleMeta meta = module.getMeta();

        if (modules.containsKey(meta.getName())) throw new IllegalArgumentException("Module with name " + meta.getName() + "already loaded");

        String moduleName = meta.getName();

        if (!module.downloadLibraries()) throw new RuntimeException("Library download failed");

        module.loadModule();

        module.initialize();

        modules.put(moduleName, module);

        return module;

    }

    @Override
    public synchronized void unloadModule(@NotNull IModule imodule) {

        checkValid();

        Module module = checkModuleValid(imodule);

        if (module.isEnabled()) disableModule(module);

        String moduleName = module.getMeta().getName();

        try {
            module.getMain().onUnload();
        }

        catch (Throwable t) {
            logger.error("Error occurred while unloading {}.", moduleName);
        }

        module.closeOrReport(logger);

        modules.remove(moduleName);

    }

    /* ENABLE/DISABLE */

    @Override
    public @Nullable IModule getModule(@NotNull String name) {
        return modules.get(name);
    }

    @Override
    public synchronized void enableModule(@NotNull IModule imodule) {

        Module module = checkModuleValid(imodule);
        if (module.isEnabled()) return;

        logger.info("Enabling {} v{}...", module.getName(), module.getMeta().getVersion());

        try {
            module.setEnabled(true);
        }

        catch (Throwable t) {
            logger.error("An error occurred while enabling {}.", module.getName(), t);
        }

    }

    @Override
    public synchronized void disableModule(@NotNull IModule imodule) {

        Module module = checkModuleValid(imodule);
        if (!module.isEnabled()) return;

        logger.info("Disabling {} v{}...", module.getName(), module.getMeta().getVersion());

        try {
            module.setEnabled(false);
        }

        catch (Throwable t) {
            logger.error("An error occurred while disabling {}.", module.getName(), t);
        }

    }

    /* utils */

    private @NotNull List<File> searchForModulesFiles() {
        checkValid();
        return Arrays.stream(Objects.requireNonNull(modulesDir.listFiles())).filter(file -> file.getName().endsWith(".jar")).toList();
    }

    private @NotNull List<Module> prepareModules(@NotNull List<File> files) {

        checkValid();

        List<Module> out = new ArrayList<>();

        for (File file : files) {

            try {

                Module loader = new Module(this, file);

                loader.readMeta();

                out.add(loader);

            }

            catch (IOException e) {
                logger.error("Failed to load .jar {}.", file.getName(), e);
            }

            catch (InvalidModuleMetaException e) {
                logger.error("Invalid module .jar {}.", file.getName(), e);
            }

        }

        return out;

    }

    public @NotNull Module checkModuleValid(@NotNull IModule module) {
        checkValid();

        Objects.requireNonNull(module, "module == null");

        if (!(module instanceof Module m)) {
            throw new IllegalArgumentException("IModule object is instance of `" + module.getClass().getName() + "` not a net.survivalboom.sbds.core.modules.Module object! Are you trying to break SBDS?");
        }

        if (!modules.containsValue(m)) throw new IllegalArgumentException("Module object is not registered in the ModuleManager. Is module unloaded?");
        if (!m.valid()) throw new IllegalArgumentException("Module object is registered in ModuleManager, but Method#valid returned false. Did you break something?");

        return m;

    }

    public @NotNull Module checkModuleEnabled(@NotNull IModule imodule, @Nullable String message) {

        Module module = checkModuleValid(imodule);
        if (!module.isEnabled()) {
            String msg = message != null ? message : "Module must be enabled";
            throw new IllegalArgumentException(msg);
        }

        return module;

    }

    public static @NotNull Module convertIModule(@NotNull IModule iModule) {

        if (iModule instanceof Module module) {
            return module;
        }

        throw new IllegalArgumentException("IModule object `" + iModule.getName() + "` is not a real Module object.");

    }


    //
    // GETTERS
    //

    public @NotNull SBDS getSbds() {
        checkValid();
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

    public @NotNull ModulesClasspath getModulesClasspath() {
        checkValid();
        return modulesClasspath;
    }

}
