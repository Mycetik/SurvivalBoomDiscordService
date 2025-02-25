package net.survivalboom.sbds.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.survivalboom.sbds.api.database.IDatabase;
import net.survivalboom.sbds.api.database.IRepository;
import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.modules.Module;
import org.bspfsystems.yamlconfiguration.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class Database extends Manager implements IDatabase {

    private static final Logger log = LoggerFactory.getLogger("Database");

    private final SBDS sbds;

    @Nullable private HikariDataSource dataSource = null;


    private final Map<NamespacedKey, Repository> repositoryMap = new HashMap<>();


    public Database(@NotNull SBDS sbds) {
        this.sbds = sbds;
    }


    @Override
    protected void init0() {

        log.info("Loading database...");

        try {
            reload0();
        }

        catch (Throwable t) {
            log.error("Failed to initialize the database. Please ensure that database credentials are correct and your database is online.");
            throw t;
        }

    }

    @Override
    protected void shutdown0() {
        log.info("Shutting down database...");
        if (dataSource != null) dataSource.close();
    }

    //
    // RELOAD
    //

    @Override
    public void reload(@NotNull IModule module) {
        Objects.requireNonNull(module, "module == null");
        reload0(module);
    }

    public void reload0(@Nullable IModule module) {

        checkValid();

        if (module != null) {
            sbds.getModuleManager().checkModuleEnabled(module, "Disabled module attempted to request a database reload");
            log.info("Module {} requested a database reload. Reloading the database!", module);
        }

        else log.info("Reloading database!");

        try {
            reload0();
        }

        catch (Throwable t) {
            log.error("Failed to reload the database! SBDS will not function properly!");
            log.error("All calls to the database will cause an IllegalStateException. Everything that works with the database will break!", t);
            return;
        }

        log.info("Database reloaded successfully!");

    }

    private void reload0() {

        checkValid();

        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }

        Configuration config = sbds.getConfiguration();

        String username = config.getString("database.user");
        String password = config.getString("database.password");

        String jdbcUrl = config.getString("database.jdbc");
        if (jdbcUrl == null || jdbcUrl.isBlank()) {

            String dbName = config.getString("database.database", "null");
            String dbHost = config.getString("database.host", "null");

            jdbcUrl = String.format("jdbc:postgresql://%s/%s", dbHost, dbName);

        }

        int poolSize = config.getInt("database.hikari.pool-size", 10);
        int minIdle = config.getInt("database.hikari.min-idle", 2);
        int idleTimeout = config.getInt("database.hikari.idle-timeout", 30000);
        int maxLifetime = config.getInt("database.hikari.lifetime", 1800000);
        int connectionTimeout = config.getInt("database.hikari.connection-timeout", 3000);
        String driverClassName = config.getString("database.hikari.driver", "org.postgresql.Driver");

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(jdbcUrl);
        if (username != null) hikariConfig.setUsername(username);
        if (password != null) hikariConfig.setPassword(password);

        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setMinimumIdle(minIdle);
        hikariConfig.setIdleTimeout(idleTimeout);
        hikariConfig.setMaxLifetime(maxLifetime);
        hikariConfig.setConnectionTimeout(connectionTimeout);
        hikariConfig.setDriverClassName(driverClassName);

        dataSource = new HikariDataSource(hikariConfig);

        getRepositories0().forEach(Repository::reload);

    }

    //
    // REPOSITORIES
    //

    @Override
    public @NotNull Repository createRepository(@NotNull IModule module, @NotNull String name, @NotNull RepositoryHandler handler) {

        Objects.requireNonNull(module, "module == null");
        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(handler, "handler == null");

        NamespacedKey namespacedKey = NamespacedKey.fromModule(module, name);

        return createRepository0(module, namespacedKey, handler);

    }

    public synchronized @NotNull Repository createRepository0(@Nullable IModule iModule, @NotNull NamespacedKey namespacedKey, @NotNull RepositoryHandler handler) {

        if (repositoryMap.containsKey(namespacedKey)) throw new IllegalArgumentException("Repository with name `" + namespacedKey + "` already exists");

        Repository repository;
        if (iModule != null) {
            Module module = sbds.getModuleManager().checkModuleEnabled(iModule, "Disabled module attempted to create a repository");
            repository = new Repository(module, namespacedKey, this);
            module.getRegistration().add("Repository-" + namespacedKey.key(), () -> removeRepository(repository));
        }

        else repository = new Repository(null, namespacedKey, this);

        repository.configure(handler);
        repository.reload();

        repositoryMap.put(namespacedKey, repository);

        return repository;

    }

    @Override
    public void removeRepository(@NotNull String name) {
        removeRepository(NamespacedKey.fromString(name));
    }

    @Override
    public void removeRepository(@NotNull IRepository repository) {
        removeRepository(repository.getName());
    }

    @Override
    public void removeRepository(@NotNull NamespacedKey key) {

        Repository repository = repositoryMap.get(key);
        if (repository == null) return;

        repository.invalid();
        repositoryMap.remove(key);

    }


    @Override
    public @NotNull List<IRepository> getRepositories() {
        return new ArrayList<>(getRepositories0());
    }

    public @NotNull List<Repository> getRepositories0() {
        return new ArrayList<>(repositoryMap.values());
    }

    @Override
    public @NotNull List<IRepository> getRepositories(@NotNull IModule module) {
        return new ArrayList<>(getRepositories0(module));
    }

    public @NotNull List<Repository> getRepositories0(@NotNull IModule module) {
        Objects.requireNonNull(module, "module == null");
        List<Repository> repositories = getRepositories0();
        repositories.removeIf(r -> !module.equals(r.getModule()));
        return repositories;
    }


    @Override
    public @Nullable IRepository getRepository(@NotNull String name) {
        return getRepository(NamespacedKey.fromString(name));
    }

    @Override
    public @Nullable IRepository getRepository(@NotNull NamespacedKey key) {
        return repositoryMap.get(key);
    }


    @Override
    public @Nullable <T> T getRepositoryHandler(@NotNull String name, @NotNull Class<T> cast) {
        return getRepositoryHandler(NamespacedKey.fromString(name), cast);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T> T getRepositoryHandler(@NotNull NamespacedKey key, @NotNull Class<T> cast) {

        IRepository repository = getRepository(key);
        if (repository == null) return null;

        return (T) repository.getHandler();

    }


    //
    // CONNECTION
    //

    public @NotNull Connection requestConnection(@NotNull Repository repository) throws SQLException {

        checkValid();
        checkRepository(repository);

        if (dataSource == null) throw new IllegalStateException("Datasource is not present. Looks like database was reloaded incorrectly.");

        return dataSource.getConnection();

    }

    //
    // UTILS
    //

    private void checkRepository(@NotNull Repository repository) {

        if (!repositoryMap.containsValue(repository)) throw new IllegalArgumentException("Repository object is not registered in the Database. Looks like this repository object is no longer valid.");
        if (!repository.valid()) throw new IllegalArgumentException("Repository object is registered, but method Repository#valid returned false. Did you break something?");

    }

    private void checkDatasource() {
        Objects.requireNonNull(dataSource, "Datasource == null");
    }


    record RegisteredRepository(@Nullable Module module, @NotNull RepositoryHandler repository) {}

}
