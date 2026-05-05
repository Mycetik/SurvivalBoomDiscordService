package net.survivalboom.sbds.core.database;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import net.survivalboom.sbds.api.database.DataRecord;
import net.survivalboom.sbds.api.database.IDatabase;
import net.survivalboom.sbds.api.database.IRepository;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.scheduler.ISchedulerTask;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.database.guilds.GuildDataRecord;
import net.survivalboom.sbds.core.database.users.UserDataRecord;
import net.survivalboom.sbds.core.registration.InternalRegistrationManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

public class Database extends Manager implements IDatabase {

    private static final Logger log = LoggerFactory.getLogger("Database");

    private final SBDS sbds;


    private @Nullable Properties properties = null;
    private @Nullable SessionFactory sessionFactory = null;


    private final InternalRegistrationManager<IRepository<?>> registry;

    private final DatabaseQueue queue;

    // repo queue //

    private final Queue<IRepository<?>> repoRebuildQueue = new ConcurrentLinkedQueue<>();

    private ISchedulerTask rebuildQueue = null;


    public Database(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.registry = new InternalRegistrationManager<>(this, "database", null, sbds.getRegistrationRegistry());
        this.queue = new DatabaseQueue(this, sbds.getScheduler());
    }

    //
    // MANAGER
    //

    @Override
    protected void init0() {

        log.info("Loading database...");

        new File(sbds.getWorkingDir(), "data").mkdirs();

        createRepository0(null, "users", UserDataRecord.class);
        createRepository0(null, "guilds", GuildDataRecord.class);

        queue.init();

        rebuildQueue = sbds.getScheduler().schedule0(null, "RebuildSessionQueue", task -> rebuildQueueTask(), 1000, 500);

    }

    @Override
    protected void shutdown0() {

        log.info("Shutting down database...");

        rebuildQueue.cancel();
        rebuildQueue = null;

        registry.shutdown();
        queue.shutdown();

        if (sessionFactory != null) {
            sessionFactory.close();
        }

    }

    // REBUILD QUEUE //

    private void rebuildQueueTask() {

        List<IRepository<?>> queue = new ArrayList<>(this.repoRebuildQueue);
        this.repoRebuildQueue.clear();

        try {
            rebuildSessionFactory(queue);
        }

        catch (Throwable t) {
            log.error("Failed to initialize the database. Please ensure that database credentials are correct and your database is online.");
            throw t;
        }

    }

    // RELOAD //

    @Override
    public void reload(@NotNull IModule module) {
        Objects.requireNonNull(module, "module == null");
        reload0(module, false);
    }

    public void reload0(@Nullable IModule module, boolean silent) {

        checkValid();

        if (module != null) {

            sbds.getModuleManager().checkModuleEnabled(module, "Disabled module attempted to request a database reload");

            if (!silent) {
                log.warn("Module {} requested a database reload. Reloading the database!", module);
            }

        }

        else if (!silent) {
            log.warn("Requested a database reload! Reloading the database!");
        }

        try {

            loadProperties();

        }

        catch (Exception t) {
            log.error("Failed to reload the database! SBDS will not function properly!");
            log.error("All calls to the database will cause an IllegalStateException. Everything that works with the database will break!", t);
            return;
        }

        if (!silent) {
            log.info("Database reloaded successfully!");
        }

    }

    private void loadProperties() {

        this.properties = null;

        ConfigurationNode config = sbds.getConfiguration();

        ConfigurationNode databaseSection = config.node("database");

        String jdbcUrl = databaseSection.node("jdbc").getString("null")
                .replace("{SBDS-DIR}", sbds.getWorkingDir().getAbsolutePath());

        String driver = databaseSection.node("driver").getString("null");
        String dialect = databaseSection.node("dialect").getString();
        String tableModifier = databaseSection.node("mode").getString("none");

        String username = databaseSection.node("user").getString();
        String password = databaseSection.node("password").getString();


        Properties properties = CommonUtils.getPropertiesFromYaml(databaseSection.node("properties"));

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

    private void rebuildSessionFactory(@NotNull Collection<IRepository<?>> toImport) {

        Objects.requireNonNull(properties, "properties == null");

        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }

        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
        configuration.setProperties(properties);

        for (var repo : toImport) {
            Class<?> clazz = repo.getRecordClass();
            configuration.addAnnotatedClass(clazz);
        }

        sessionFactory = configuration.buildSessionFactory();

    }

    //
    // REPOSITORIES
    //

    // CREATE //

    @Override
    public <T extends DataRecord> @NotNull Repository<T> createRepository(@NotNull IModule module, @NotNull String name, @NotNull Class<T> clazz) {

        Objects.requireNonNull(module, "module == null");
        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(clazz, "clazz == null");

        return createRepository0(module, name, clazz);

    }

    @SuppressWarnings("unchecked")
    public synchronized <T extends DataRecord> @NotNull Repository<T> createRepository0(@Nullable IModule module, @NotNull String name, @NotNull Class<T> clazz) {

        checkValid();

        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("DataRecord class must have jakarta.persistence.Entity annotation");
        }

        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalArgumentException("RepositoryHandler's DataRecord class must have jakarta.persistence.Table annotation");
        }

        String tableName = table.name();
        boolean tableExists = registry.getRegistrations().stream()
                .map(Registration::object)
                .map(IRepository::getRecordClass)
                .anyMatch(cls -> cls.getAnnotation(Table.class).name().equals(tableName));

        if (tableExists) {
            throw new IllegalArgumentException("DataRecord with table name `" + tableName + "` already exist");
        }

        if (module != null) {
            sbds.getModuleManager().checkModuleEnabled(module, "Disabled module attempted to create a repository");
        }

        Repository<T> repository = new Repository<>(clazz, this);
        repository.registration = (Registration<IRepository<T>>) (Registration<?>) registry.register0(module, name, repository);

        repoRebuildQueue.add(repository);

        return repository;

    }

    // REMOVE //


    @Override
    public synchronized boolean removeRepository(@NotNull IRepository<?> repository) {

        checkValid();

        var reg = registry.unregister(repository);

        return reg != null;

    }

    // GETTERS //

    @Override
    public @Nullable IRepository<?> getRepository(@NotNull NamespacedKey key) {

        checkValid();

        var reg = registry.getRegistration(key);
        if (reg == null) {
            return null;
        }

        return reg.object();

    }

    @Override
    public @NotNull List<IRepository<?>> getRepositories() {
        return registry.getRegisteredObjects();
    }

    //
    // SESSIONS
    //

    public @NotNull Session requestSession(@NotNull Repository<?> repository) {

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
    // QUEUE
    //

    @Override
    public void queueSave(@NotNull DataRecord record) {
        checkRepository(record);
        queue.queueRecordSave(record);
    }

    public @NotNull <V> CompletableFuture<V> queueSessionRequest(@NotNull Repository<?> repository, @NotNull Function<Session, V> function) {

        checkValid();
        checkRepository(repository);
        checkDatabase();

        return queue.queueSessionRequest(function);

    }

    public @NotNull CompletableFuture<Void> queueSessionRequest(@NotNull Repository<?> repository, @NotNull Consumer<Session> consumer) {

        checkValid();
        checkRepository(repository);
        checkDatabase();

        return queue.queueSessionRequest(consumer);

    }

    public @NotNull DatabaseQueue getQueue() {
        return queue;
    }

    //
    // UTILS
    //

    public void checkRepository(@NotNull DataRecord record) {

        Objects.requireNonNull(record, "record == null");

        if (registry.getRegistrations().stream().noneMatch(reg -> reg.object().getRecordClass().equals(record.getClass()))) {
            throw new IllegalArgumentException("Repository object is not registered in the Database. Looks like this repository object is no longer valid.");
        }

    }

    private void checkRepository(@NotNull Repository<?> repository) {

        if (registry.getObjectRegistration(repository) != null) {
            throw new IllegalArgumentException("Repository object is not registered in the Database. Looks like this repository object is no longer valid.");
        }

        if (!repository.isValid()) {
            throw new IllegalArgumentException("Repository object is registered, but method Repository#valid returned false. Did you break something?");
        }

    }

    private void checkDatabase() {
        Objects.requireNonNull(sessionFactory, "Datasource is not present. Looks like database was reloaded incorrectly.");
    }

}
