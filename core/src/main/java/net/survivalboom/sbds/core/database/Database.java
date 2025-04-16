package net.survivalboom.sbds.core.database;

import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.database.IDatabase;
import net.survivalboom.sbds.api.database.IRepository;
import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.database.guilds.GuildRepositoryHandler;
import net.survivalboom.sbds.core.database.users.UserRepositoryHandler;
import net.survivalboom.sbds.core.modules.Module;
import org.bspfsystems.yamlconfiguration.configuration.Configuration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class Database extends Manager implements IDatabase {

    private static final Logger log = LoggerFactory.getLogger("Database");

    private final SBDS sbds;


    @Nullable private Properties properties;

    @Nullable private SessionFactory sessionFactory = null;


    private final Map<NamespacedKey, Repository> repositoryMap = new HashMap<>();

    private final DatabaseSaveQueue saveQueue;


    public Database(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.saveQueue = new DatabaseSaveQueue(this, sbds);
    }


    @Override
    protected void init0() {

        log.info("Loading database...");

        new File(sbds.getWorkingDir(), "data").mkdirs();

        reloadHibernate();

        createRepository0(null, NamespacedKey.sbds("users"), new UserRepositoryHandler(), false);
        createRepository0(null, NamespacedKey.sbds("guilds"), new GuildRepositoryHandler(), false);

        try {
            rebuildSessionFactory();
        }

        catch (Throwable t) {
            log.error("Failed to initialize the database. Please ensure that database credentials are correct and your database is online.");
            throw t;
        }

        saveQueue.init0();

    }

    @Override
    protected void shutdown0() {
        log.info("Shutting down database...");
        repositoryMap.clear();
        saveQueue.shutdown0();
        if (sessionFactory != null) sessionFactory.close();
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

        try {

            log.info("Reloading Hibernate...");
            reloadHibernate();

            log.info("Reloading repositories...");
            rebuildSessionFactory();
            getRepositories0().forEach(Repository::reload);

        }

        catch (Throwable t) {
            log.error("Failed to reload the database! SBDS will not function properly!");
            log.error("All calls to the database will cause an IllegalStateException. Everything that works with the database will break!", t);
            return;
        }

        log.info("Database reloaded successfully!");

    }

    private void reloadHibernate() {

        if (sessionFactory != null) {
            properties = null;
            sessionFactory.close();
            sessionFactory = null;
        }

        Configuration config = sbds.getConfiguration();

        String jdbcUrl = config.getString("database.jdbc", "null").replace("{SBDS-DIR}", sbds.getWorkingDir().getAbsolutePath());
        String driver = config.getString("database.driver", "null");
        String dialect = config.getString("database.dialect");
        String tableModifier = config.getString("database.table-modify", "none");

        String username = config.getString("database.user");
        String password = config.getString("database.password");



        Properties properties = CommonUtils.getPropertiesFromYaml(CommonUtils.getOrCreateSection(config, "database.properties"));

        properties.setProperty("hibernate.connection.url", jdbcUrl);

        properties.setProperty("hibernate.connection.driver_class", driver);
        if (dialect != null) properties.setProperty("hibernate.dialect", dialect);
        properties.setProperty("hibernate.hbm2ddl.auto", tableModifier);

        if (username != null) properties.setProperty("hibernate.connection.username", username);
        if (password != null) properties.setProperty("hibernate.connection.password", password);

        properties.setProperty("hibernate.hikari.poolName", "DatabaseHikariMain");
//        properties.setProperty("hibernate.connection.provider_class", "com.zaxxer.hikari.hibernate.HikariConnectionProvider");
        properties.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");

        this.properties = properties;

    }

    private void rebuildSessionFactory() {

        Objects.requireNonNull(properties, "properties == null");

        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }

        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
        configuration.setProperties(properties);

        for (Repository repository : repositoryMap.values()) {
            Class<?> clazz = repository.getHandler().getDataRecordClass();
            configuration.addAnnotatedClass(clazz);
        }

        sessionFactory = configuration.buildSessionFactory();

    }

    //
    // REPOSITORIES
    //

    @Override
    public @NotNull Repository createRepository(@NotNull IModule module, @NotNull String name, @NotNull RepositoryHandler<?> handler) {

        Objects.requireNonNull(module, "module == null");
        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(handler, "handler == null");

        NamespacedKey namespacedKey = NamespacedKey.fromModule(module, name);

        return createRepository0(module, namespacedKey, handler, true);

    }

    public synchronized @NotNull Repository createRepository0(@Nullable IModule iModule, @NotNull NamespacedKey namespacedKey, @NotNull RepositoryHandler<?> handler, boolean rebuiltSessionFactory) {

        if (repositoryMap.containsKey(namespacedKey)) throw new IllegalArgumentException("Repository with name `" + namespacedKey + "` already exists");

        Repository repository;
        if (iModule != null) {
            Module module = sbds.getModuleManager().checkModuleEnabled(iModule, "Disabled module attempted to create a repository");
            repository = new Repository(module, namespacedKey, this);
            module.getRegistration().add("Repository-" + namespacedKey.key(), () -> removeRepository(repository));
        }

        else repository = new Repository(null, namespacedKey, this);

        repositoryMap.put(namespacedKey, repository);

        repository.configure(handler);
        if (rebuiltSessionFactory) rebuildSessionFactory();
        repository.reload();

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
    public synchronized void removeRepository(@NotNull NamespacedKey key) {

        Repository repository = repositoryMap.get(key);
        if (repository == null) return;

        repository.invalid();
        repositoryMap.remove(key);

        rebuildSessionFactory();

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

        return (T) Objects.requireNonNull(repository.getHandler(), "handler == null, something went wrong");

    }


    //
    // SESSIONS
    //

    public @NotNull Session requestSession(@NotNull Repository repository) {

        checkValid();
        checkRepository(repository);
        checkDatabase();

        return createSession();

    }

    public @NotNull Session createSession() {
        Objects.requireNonNull(sessionFactory, "sessionFactory == null, something went wrong");
        return sessionFactory.openSession();
    }


    //
    // SAVE QUEUE
    //

    @Override
    public void queueSave(@NotNull DataRecord record) {
        saveQueue.queue(record);
    }

    //
    // UTILS
    //

    private void checkRepository(@NotNull Repository repository) {

        if (!repositoryMap.containsValue(repository)) throw new IllegalArgumentException("Repository object is not registered in the Database. Looks like this repository object is no longer valid.");
        if (!repository.valid()) throw new IllegalArgumentException("Repository object is registered, but method Repository#valid returned false. Did you break something?");

    }

    private void checkDatabase() {
        Objects.requireNonNull(sessionFactory, "Datasource is not present. Looks like database was reloaded incorrectly.");
    }

}
