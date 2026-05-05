package net.survivalboom.sbds.api.database;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IDatabase extends IManager {

    //
    // DATABASE
    //

    void reload(@NotNull IModule module);

    void queueSave(@NotNull DataRecord record);

    //
    // REPOSITORIES
    //

    // CREATION //

    @NotNull <T extends DataRecord> IRepository<T> createRepository(@NotNull IModule module, @NotNull String name, @NotNull Class<T> clazz);

    // REMOVE //

    boolean removeRepository(@NotNull IRepository<?> repository);

    default @Nullable IRepository<?> removeRepository(@NotNull NamespacedKey key) {

        IRepository<?> repository = getRepository(key);
        if (repository == null) {
            return null;
        }

        removeRepository(repository);

        return repository;

    }

    default @Nullable IRepository<?> removeRepository(@NotNull String key) {
        return removeRepository(NamespacedKey.fromString(key));
    }

    // GETTERS //

    @Nullable IRepository<?> getRepository(@NotNull NamespacedKey key);

    default @Nullable IRepository<?> getRepository(@NotNull String name) {
        return getRepository(NamespacedKey.fromString(name));
    }

    @NotNull List<IRepository<?>> getRepositories();

}
