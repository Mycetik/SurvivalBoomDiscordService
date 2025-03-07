package net.survivalboom.sbds.core.modules;

import net.survivalboom.sbds.api.modules.IModuleClassLoader;
import net.survivalboom.sbds.core.libraries.JarLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class ModuleClassLoader extends URLClassLoader implements IModuleClassLoader {

    private final Module module;

    private final JarLoader.DynamicClassLoader rootClassLoader;

    private final ModulesClasspath modulesClasspath;

    private final Map<String, Class<?>> classes = new HashMap<>();

    public ModuleClassLoader(@NotNull Module module) throws MalformedURLException {
        super(module.getName(), new URL[]{module.getFile().toURI().toURL()}, module.getClass().getClassLoader());
        this.module = module;
        this.modulesClasspath = module.getModuleManager().getModulesClasspath();
        this.rootClassLoader = module.getSbds().getLibrariesManager().getJarLoader().getClassLoader();
        modulesClasspath.register(this);
    }


    //
    // BEHAVIOUR
    //

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = loadClass0(name, resolve);
        classes.put(name, clazz);
        return clazz;
    }

    private Class<?> loadClass0(@NotNull String name, boolean resolve) throws ClassNotFoundException {

        Class<?> result = classes.get(name);
        if (result != null) {
            System.out.print("Found cached class `" + name + "`");
            return result;
        }

        Class<?> clazz = findLoadedClass(name);
        if (clazz != null) return clazz;

        clazz = getClass(name);
        if (clazz != null) {
            if (resolve) resolveClass(clazz);
            return clazz;
        }

        clazz = modulesClasspath.request(name, this);
        if (clazz != null) return clazz;

        clazz = rootClassLoader.loadClassWithoutModules(name, resolve);
        if (clazz != null) return clazz;

        throw new ClassNotFoundException();

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
