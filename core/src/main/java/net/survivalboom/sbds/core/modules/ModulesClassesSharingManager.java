package net.survivalboom.sbds.core.modules;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.dependencies.ModuleDependency;
import net.survivalboom.sbds.api.utils.valid.Manager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ModulesClassesSharingManager extends Manager {

    private final ModuleManager moduleManager;

    private final Map<String, Class<?>> recentClasses = new WeakHashMap<>();


    public ModulesClassesSharingManager(@NotNull ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    //
    // MANAGER
    //

    @Override
    protected void init0() {

    }

    @Override
    protected void shutdown0() {
        recentClasses.clear();
    }

    //
    // REQUEST
    //

    public @Nullable Class<?> request(@NotNull String name, @NotNull Module module) {

        checkValid();
        moduleManager.checkModuleValid(module);

        // TODO 25.4.2026 9:28 -> Реалізувати кешування.

        // Отримуємо список усіх завантажених модулів та відсіюємо ті які не прописані у залежностях модуля що запитує клас.

        List<IModule> modules = moduleManager.getModules();
        List<ModuleDependency> dependencies = module.getMeta().getDependencies();

        modules.removeIf(m -> dependencies.stream().noneMatch(dependency -> dependency.id().equals(module.getName()) && dependency.joinClasspath()));

        // Шукаємо запитуваний клас у всіх модулях, що приписані у залежностях.

        Class<?> clazz = null;
        for (IModule depModule : modules) {

            Module m = (Module) depModule;

            clazz = m.getClassLoader().getClass(name, false, false, false);

        }

        recentClasses.put(name, clazz); // Не забуваємо додати у кеш результат. Навіть якщо null.

        return clazz;

    }


}
