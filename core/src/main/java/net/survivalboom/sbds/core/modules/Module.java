package net.survivalboom.sbds.core.modules;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.InvalidModuleException;
import net.survivalboom.sbds.api.modules.InvalidModuleMetaException;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.api.utils.Valid;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class Module extends Valid implements IModule {

    private final File file;

    private final JarFile jarFile;

    private File dataDir;


    private final ModuleManager moduleManager;

    private final ModuleRegistration registration = new ModuleRegistration();


    private final YamlConfiguration yamlConfiguration = new YamlConfiguration();


    private final SBDS sbds;


    private ModuleMeta meta;

    private ModuleClassLoader classLoader;

    private ModuleMain moduleMain;


    private Logger logger = null;


    private boolean enabled = false;


    public Module(@NotNull ModuleManager moduleManager, @NotNull File file) throws IOException {
        this.file = file;
        this.jarFile = new JarFile(file);
        this.moduleManager = moduleManager;
        this.sbds = moduleManager.getSbds();
    }


    public @NotNull ModuleMeta readMeta() throws InvalidModuleMetaException, IOException {

        checkValid();

        if (meta != null) return meta;

        ZipEntry entry = jarFile.getEntry("module.yml");
        if (entry == null) throw new InvalidModuleMetaException("File 'module.yml' not found in jar");

        try (InputStream stream = jarFile.getInputStream(entry)) {
            meta = ModuleMeta.loadFrom(stream);
        }

        logger = LoggerFactory.getLogger(getName());
        dataDir = new File(moduleManager.getModulesDir(), getName());

        return meta;

    }

    public boolean downloadLibraries() {

        ConfigurationSection librariesSection = meta.getLibrariesSection();
        if (librariesSection == null) return true;

        logger.info("Loading libraries...");
        boolean success = sbds.getLibrariesManager().satisfy0(this, librariesSection, true);

        if (!success) logger.error("Failed to load libraries. Refusing to load the module!");

        return success;

    }

    public @NotNull ModuleMain loadModule() throws InvalidModuleException {

        checkValid();

        if (moduleMain != null) return moduleMain;
        Objects.requireNonNull(meta, "meta == null");

        try {
            classLoader = new ModuleClassLoader(this);
        }

        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try {
            moduleMain = createModuleObject(meta, classLoader);
        }

        catch (Throwable t) {
            classLoader.closeOrReport(logger);
            throw t;
        }

        return moduleMain;

    }


    public void initialize() throws InvalidModuleException {

        checkValid();

        Objects.requireNonNull(moduleMain, "module == null");

        moduleMain.init(this);

        try {
            moduleMain.onLoad();
        }

        catch (Throwable t) {
            throw new InvalidModuleException(t.getMessage(), t);
        }

    }


    private @NotNull ModuleMain createModuleObject(@NotNull ModuleMeta meta, @NotNull ModuleClassLoader classLoader) throws InvalidModuleException {

        Class<?> clazz = classLoader.getClass(meta.getMain());
        Constructor<? extends ModuleMain> constructor = getConstructor(meta, clazz);


        try {
            return constructor.newInstance();
        }

        catch (IllegalAccessException e) {
            throw new InvalidModuleException("main class `" + meta.getMain() + "` constructor must be public");
        }

        catch (InstantiationException e) {
            throw new InvalidModuleException("main class`" + meta.getMain() + "` must not be abstract");
        }

        catch (IllegalArgumentException e) {
            throw new InvalidModuleException("Could not invoke main class `" + meta.getMain() + "` constructor", e);
        }

        catch (ExceptionInInitializerError | InvocationTargetException e) {
            throw new InvalidModuleException("Exception initializing main class `" + meta.getMain() + "`", e);
        }

    }

    private @NotNull Constructor<? extends ModuleMain> getConstructor(@NotNull ModuleMeta meta, Class<?> clazz) throws InvalidModuleException {

        if (clazz == null) throw new InvalidModuleException("Cannot find main class '" + meta.getMain() + "'");

        Class<? extends ModuleMain> moduleClass;
        try {
            moduleClass = clazz.asSubclass(ModuleMain.class);
        } catch (ClassCastException e) {
            throw new InvalidModuleException("main class `" + meta.getMain() + "` must extend ModuleMain");
        }

        Constructor<? extends ModuleMain> constructor;
        try {
            constructor = moduleClass.getDeclaredConstructor();
        }
        catch (NoSuchMethodException e) {
            throw new InvalidModuleException("main class `" + meta.getMain() + "` must have a public no-args constructor");
        }

        return constructor;

    }

    public void setEnabled(boolean v) {

        if (v == enabled) return;
        this.enabled = v;

        if (!enabled) {
            moduleMain.onDisable();
            registration.unregister();
        }

        else {

            try {
                moduleMain.onEnable();
            }

            catch (Throwable t) {
                enabled = false;
                throw t;
            }

        }

    }


    public void close() throws IOException {
        valid(false);
        if (classLoader != null) classLoader.close();
        jarFile.close();
    }

    public void closeOrReport(@NotNull Logger logger) {

        try {
            close();
        }

        catch (Throwable t) {
            logger.error("Failed to close module `{}` object properly. This may cause a memory leak!", getName(), t);
        }

    }

    @Override
    public @NotNull String getName() {
        return getMeta().getName();
    }


    @Override
    public boolean isEnabled() {
        return enabled;
    }


    @Override
    public @NotNull File getFile() {
        return file;
    }

    @Override
    public @NotNull File getDataFolder() {
        return dataDir;
    }

    @Override
    public @NotNull JarFile getJar() {
        return jarFile;
    }

    @Override
    public @NotNull ModuleManager getModuleManager() {
        return moduleManager;
    }

    @Override
    public @NotNull YamlConfiguration getConfig() {
        return yamlConfiguration;
    }


    @Override
    public @NotNull Logger getLogger() {
        Objects.requireNonNull(logger, "Module meta was not loaded yet");
        return logger;
    }

    @Override
    public @NotNull ModuleMeta getMeta() {
        Objects.requireNonNull(meta, "Module meta was not loaded yet");
        return meta;
    }

    @Override
    public @NotNull ModuleClassLoader getClassLoader() {
        Objects.requireNonNull(meta, "Module was not loaded yet");
        return classLoader;
    }

    @Override
    public @NotNull ModuleMain getMain() {
        Objects.requireNonNull(moduleMain, "Module was not loaded yet");
        return moduleMain;
    }

    @Override
    public @NotNull SBDS getSbds() {
        return sbds;
    }

    @Override
    public String toString() {
        return getName() + " v" + meta.getVersion();
    }

    public @NotNull ModuleRegistration getRegistration() {
        return registration;
    }

}
