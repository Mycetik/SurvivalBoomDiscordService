package net.survivalboom.sbds.core.modules;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ModulesClasspath {

    private final Set<ModuleClassLoader> classLoaders = new HashSet<>();

    public void register(@NotNull ModuleClassLoader classLoader) {

        Objects.requireNonNull(classLoader, "classLoader == null");

        if (classLoaders.stream().anyMatch(cl -> cl.getName().equals(classLoader.getName()))) {
            throw new IllegalStateException("Classloader with name " + classLoader.getName() + " already registered!");
        }

        classLoaders.add(classLoader);
    }

    public void unregister(@NotNull ModuleClassLoader classLoader) {
        classLoaders.remove(classLoader);
    }

    public @NotNull List<ModuleClassLoader> getClassLoaders() {
        return new ArrayList<>(classLoaders);
    }

    public @NotNull Class<?> request(@NotNull String name, @NotNull ModuleClassLoader requester) throws ClassNotFoundException {

        ModuleMeta meta = requester.getModule().getMeta();

        Class<?> clazz;
        for (ModuleMeta.Dependency dependency : meta.getDependencies()) {

            if (!dependency.joinClasspath()) continue;

            clazz = findClassInDependency(name, dependency, requester);
            if (clazz != null) return clazz;

        }

        throw new ClassNotFoundException();

    }

    private @Nullable Class<?> findClassInDependency(@NotNull String name, @NotNull ModuleMeta.Dependency dependency, @NotNull ModuleClassLoader ignore) {

        Optional<ModuleClassLoader> classLoaderOptional = classLoaders.stream().filter(cl -> cl.getName().equals(dependency.getName())).findFirst();
        if (classLoaderOptional.isEmpty()) return null;

        ModuleClassLoader classLoader = classLoaderOptional.get();

        if (classLoader.equals(ignore)) return null;

        return classLoader.getClass(name);

    }




}
