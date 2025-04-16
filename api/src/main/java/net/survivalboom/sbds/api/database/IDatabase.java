package net.survivalboom.sbds.api.database;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IDatabase {

    void reload(@NotNull IModule module);

    void queueSave(@NotNull DataRecord record);


    /*
        REPOSITORIES
     */

    @NotNull IRepository createRepository(@NotNull IModule module, @NotNull String name, @NotNull RepositoryHandler<?> handler);

    void removeRepository(@NotNull String name);

    void removeRepository(@NotNull NamespacedKey key);

    void removeRepository(@NotNull IRepository repository);


    @NotNull List<IRepository> getRepositories();

    @NotNull List<IRepository> getRepositories(@NotNull IModule module);


    @Nullable IRepository getRepository(@NotNull String name);

    @Nullable IRepository getRepository(@NotNull NamespacedKey key);


    @Nullable <T> T getRepositoryHandler(@NotNull String name, @NotNull Class<T> cast);

    @Nullable <T> T getRepositoryHandler(@NotNull NamespacedKey key, @NotNull Class<T> cast);

}
