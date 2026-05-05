package net.survivalboom.sbds.core.libraries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class DynamicClassLoader extends URLClassLoader {

    private static final Logger log = LoggerFactory.getLogger(DynamicClassLoader.class);


    private final Set<File> sources = new HashSet<>();

    private final Set<ClassSupplier> suppliers = new HashSet<>();

    private final Map<String, ClassSupplier> definedClassSuppliers = new HashMap<>();


    private boolean wasClosed = false;


    public DynamicClassLoader(@NotNull String name, @Nullable ClassLoader parent) {
        super(name, new URL[0], parent);
    }

    //
    // SOURCES
    //

    public void addSource(@NotNull File file) {

        Objects.requireNonNull(file, "file == null");
        checkValid();

        if (sources.contains(file)) {
            throw new IllegalStateException("File `" + file.getPath() + "` already registered in this ClassLoader");
        }

        URL url;
        try {
            url = file.toURI().toURL();
        }

        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        addURL(url);
        sources.add(file);

    }

    public @NotNull List<File> getSources() {
        return new ArrayList<>(sources);
    }

    //
    // SUPPLIERS
    //

    public void addClassSupplier(@NotNull ClassSupplier supplier) {

        Objects.requireNonNull(supplier, "supplier == null");
        checkValid();

        if (suppliers.contains(supplier)) {
            throw new IllegalStateException("Supplier `" + supplier + "` already registered in this class loader");
        }

        this.suppliers.add(supplier);

    }

    public boolean removeClassSupplier(@NotNull ClassSupplier supplier) {

        Objects.requireNonNull(supplier, "supplier == null");
        checkValid();

        return suppliers.remove(supplier);

    }

    public @NotNull List<ClassSupplier> getClassSuppliers() {
        checkValid();
        return new ArrayList<>(suppliers);
    }

    public void clearClassSuppliers() {
        suppliers.clear();
    }

    //
    // DEFINED CLASS SUPPLIERS
    //

    public void addDefinedClassSupplier(@NotNull String className, @NotNull ClassSupplier supplier) {

        Objects.requireNonNull(className, "className == null");
        Objects.requireNonNull(supplier, "supplier == null");
        checkValid();

        this.definedClassSuppliers.put(className, supplier);

    }

    public @Nullable ClassSupplier removeDefinedClassSupplier(@NotNull String name) {

        checkValid();

        return this.definedClassSuppliers.remove(name);

    }

    public @NotNull Map<String, ClassSupplier> getDefinedClassSuppliers() {
        return new HashMap<>(definedClassSuppliers);
    }


    //
    // CLASSES
    //

    @Override
    protected @NotNull Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        if (wasClosed) {
            throw new ClassNotFoundException("ClassLoader `" + this + "` was closed");
        }

        Class<?> clazz = getClass(name, true, true, true);

        if (clazz != null) {

            if (resolve) {
                resolveClass(clazz);
            }

            return clazz;

        }

        throw new ClassNotFoundException("Class `" + name + "was not found in `" + this + "`");

    }

    public @Nullable Class<?> getClass(
            @NotNull String name,
            boolean definedSuppliers,
            boolean suppliers,
            boolean parent
    ) {

        checkValid();

        // Шукаємо чи був цей клас вже завантажений цим об'єктом раніше. Якщо так, повертаємо його.

        Class<?> clazz = findLoadedClass(name);
        if (clazz != null) {
            return clazz;
        }

        // Шукаємо клас у sources поточного class loader.
        try {
            clazz = findClass(name);
        }

        catch (ClassNotFoundException ignored) {}


        // Шукаємо клас у defined class suppliers
        if (definedSuppliers) {

            ClassSupplier supplier = definedClassSuppliers.get(name);
            if (supplier != null) {

                try {
                    clazz = supplier.supplyClass(name);
                }

                catch (Exception e) {
                    log.error("An exception was thrown in `{}` when tried to load class `{}`.", supplier, name, e);
                }

            }

        }


        // Шукаємо клас у class suppliers
        if (suppliers) {

            if (clazz != null) {

                for (ClassSupplier sup4ik : this.suppliers) {

                    try {
                        clazz = sup4ik.supplyClass(name);
                    }

                    catch (Exception e) {
                        log.error("An exception was thrown in `{}` when tried to load class `{}`.", sup4ik, name, e);
                    }

                    break;

                }

            }

        }

        // Шукаємо клас у parent classloader
        if (parent) {

            ClassLoader parentClassLoader = getParent();
            if (clazz != null && parentClassLoader != null) {

                try {
                    clazz = parentClassLoader.loadClass(name);
                }

                catch (ClassNotFoundException ignored) {

                }

                catch (Exception e) {
                    log.error("An exception was thrown in parent class loader `{}` when tried to load class `{}`.", parent, name, e);
                }

            }

        }

        return clazz;

    }

    //
    // CLOSABLE
    //

    @Override
    public void close() {

        checkValid();

        this.wasClosed = true;

        try {
            super.close();
        }

        catch (IOException e) {
            log.warn("There was an exception thrown in URLClassLoader.close(). Maybe something went wrong. This error can cause memory leaks and weird behaviour!", e);
        }

        this.sources.clear();
        this.suppliers.clear();

    }

    public boolean isClosed() {
        return wasClosed;
    }

    private void checkValid() {
        if (wasClosed) {
            throw new IllegalStateException("classloader was closed");
        }
    }

    //
    // CUSTOM CLASS SOURCE
    //

    public interface ClassSupplier {

        @Nullable Class<?> supplyClass(@NotNull String name);

    }


    //
    // MISC
    //

    @Override
    public String toString() {
        return String.format(
                "DynamicClassLoader{name=%s, sources=%s, suppliers=%s, definedSuppliers=%s}",
                getName(),
                sources.size(),
                suppliers.size(),
                definedClassSuppliers.size()
        );
    }

}
