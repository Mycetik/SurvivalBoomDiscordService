package net.survivalboom.sbds.core.modules;

import net.survivalboom.sbds.api.modules.IModuleClassLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModuleClassLoader extends URLClassLoader implements IModuleClassLoader {

    private final Module module;

    private final JarFile jarFile;

    private final ModulesClasspath modulesClasspath;

    private final Map<String, Class<?>> classes = new HashMap<>();

    public ModuleClassLoader(@NotNull Module module) throws MalformedURLException {
        super(module.getName(), new URL[]{module.getFile().toURI().toURL()}, module.getClass().getClassLoader());
        this.module = module;
        this.jarFile = module.getJar();
        this.modulesClasspath = module.getModuleManager().getModulesClasspath();
        modulesClasspath.register(this);
    }


    //
    // BEHAVIOUR
    //

    public @Nullable Class<?> loadClassForcibly(@NotNull String name) {
        Objects.requireNonNull(name, "name == null!");
        return loadClass(this, name);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass0(name, resolve, true, false);
    }

    private Class<?> loadClass0(@NotNull String name, boolean resolve, boolean global, boolean dependencies) throws ClassNotFoundException {

        Class<?> result = classes.get(name);
        if (result != null) {
            System.out.printf("Found cached class %s\n", name);
            return result;
        }


        String path = name.replace(".", "/").concat(".class");

        JarEntry entry = jarFile.getJarEntry(path);
        Class<?> clazz = entry != null ? super.loadClass(name, resolve) : loadClass1(name, global, dependencies);

        classes.put(name, clazz);

        return clazz;

    }

    private Class<?> loadClass1(@NotNull String name, boolean global, boolean dependencies) throws ClassNotFoundException {

        Class<?> clazz = null;
        if (global) clazz = loadClass(getParent(), name);

        if (clazz == null && dependencies) {
            return modulesClasspath.request(name, this);
        }

        if (clazz == null) throw new ClassNotFoundException();

        return clazz;

    }

    private @Nullable Class<?> loadClass(@NotNull ClassLoader classLoader, @NotNull String name) {

        try {
            return classLoader.loadClass(name);
        }

        catch (ClassNotFoundException e) {
            return null;
        }

    }


    public @Nullable Class<?> getClass(@NotNull String name) {

        try {
            return findClass(name);
        }

        catch (ClassNotFoundException e) {
            return null;
        }

    }

    public @NotNull Module getModule() {
        return module;
    }

    @Override
    public void close() throws IOException {
        modulesClasspath.unregister(this);
        super.close();
    }

    public void closeOrReport(@NotNull Logger logger) {

        try {
            close();
        }

        catch (Throwable t) {
            logger.error("Failed to close ModuleClassLoader `{}`. This may cause memory leak!", getName(), t);
        }

    }

}
