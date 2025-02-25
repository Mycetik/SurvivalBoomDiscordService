package net.survivalboom.sbds.api.database;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class RepositoryHandler<T extends DataRecord> {

    protected final Set<T> cache = new HashSet<>();

    protected IRepository repository;

    public void configure(@NotNull IRepository repository) {
        this.repository = repository;
    }

    public void reload() throws Throwable {

        getCache().forEach(DataRecord::invalid);
        cache.clear();

        checkTables();

    }

    public abstract void checkTables() throws SQLException;


    public @NotNull List<T> getCache() {
        return new ArrayList<>(cache);
    }


    protected @NotNull Connection getConnection() throws SQLException {
        return repository.getConnection();
    }



}
