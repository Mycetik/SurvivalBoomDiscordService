package net.survivalboom.sbds.core.libraries;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class JarLoader extends URLClassLoader {

    private final List<File> files = new ArrayList<>();

    private Function<String, Class<?>> modulesClasspathInterface = null; // Костиль, я знаю, але я не винен що java так працює. Йдіть в сраку!

    public JarLoader(@NotNull ClassLoader parent) throws URISyntaxException {
        super("SBDSJarLoader", new URL[] {}, parent);
        mountJar(new File(JarLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
    }

    public void mountJar(@NotNull File file) {

        if (files.contains(file)) return;

        try {
            URL url = file.toURI().toURL();
            addURL(url);
        }

        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        files.add(file);

    }

    public void configure(Function<String, Class<?>> modulesClasspathInterface) {
        Objects.requireNonNull(modulesClasspathInterface);
        if (this.modulesClasspathInterface != null) throw new RuntimeException("fuck yourself!");
        this.modulesClasspathInterface = modulesClasspathInterface;
    }

    @Override
    public void addURL(@NotNull URL url) {
        super.addURL(url);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass0(name, resolve);
    }

    // Я переписав алгоритм завантаження класів, аби він спочатку намагався завантажити класи з підключених JAR.
    // Тільки якщо алгоритм не знайшов клас у JAR, він кинеться шукати клас в батьківському classloader.
    // Взагалі, це дуже погана ідея, але у цьому випадку це єдине рішення, оскільки наш завантажувач класів має бути основним
    // у програмі, тому що ми підвантажуємо усі бібліотеки динамічно.
    public Class<?> loadClassWithoutModules(@NotNull String name, boolean resolve) throws ClassNotFoundException {

        // Через те що ми підвантажуємо JarLoader у Main від імені AppClassLoader,
        // клас буде відрізнятись для JVM від класу JarLoader, який завантажить вже цей ClassLoader при ініціалізації SbdsBootstrap.
        // На жаль, не відповідає принципам ООП, але що поробиш...
        if (name.equals("net.survivalboom.sbds.core.libraries.JarLoader")) {
            return getParent().loadClass(name);
        }

        Class<?> c = findLoadedClass(name);
        if (c != null) return c;

        try {
            c = findClass(name);
        }

        catch (ClassNotFoundException e) {

            ClassLoader parent = getParent();
            if (parent == null) parent = ClassLoader.getSystemClassLoader();

            c = parent.loadClass(name);

        }

        if (resolve) {
            resolveClass(c);
        }

        return c;

    }

    protected @NotNull Class<?> loadClass0(String name, boolean resolve) throws ClassNotFoundException {

        try {
            return loadClassWithoutModules(name, resolve);
        }

        catch (ClassNotFoundException e) {
            if (modulesClasspathInterface == null) throw e;
        }

        Class<?> clazz = modulesClasspathInterface.apply(name);
        if (clazz != null) {
            System.out.println("Loaded module class `" + clazz.getName() + "`");
            return clazz;
        }

        throw new ClassNotFoundException();

    }


    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }


}
