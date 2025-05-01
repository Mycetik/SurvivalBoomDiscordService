package net.survivalboom.sbds.api.database;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class RepositoryHandler<T extends DataRecord> {

    protected final Map<Object, T> cache = new ConcurrentHashMap<>();

    protected final Class<T> dataRecordClass;

    protected IRepository repository;


    public RepositoryHandler(@NotNull Class<T> clazz) {
        this.dataRecordClass = clazz;
    }



    public void configure(@NotNull IRepository repository) {
        this.repository = repository;
    }

    public void reload() {
        getCache().forEach(DataRecord::invalid);
        cache.clear();
    }

    public @NotNull List<T> getCache() {
        return new ArrayList<>(cache.values());
    }

    protected @NotNull Session getSession() {
        return repository.getSession();
    }

    protected <V> V sessionReturn(@NotNull Function<Session, V> function) {

        Objects.requireNonNull(function, "function == null");

        try (Session session = getSession()) {
            return function.apply(session);
        }

    }

    protected void session(@NotNull Consumer<Session> consumer) {

        Objects.requireNonNull(consumer, "consumer == null");

        try (Session session = getSession()) {
            consumer.accept(session);
        }

    }

    protected @NotNull T create(@NotNull T record) {

        Objects.requireNonNull(record, "record == null");

        session(session -> {
            Transaction transaction = session.beginTransaction();
            session.persist(record);
            transaction.commit();
        });

        return record;

    }

    protected @NotNull T save(@NotNull T record) {

        Objects.requireNonNull(record, "record == null");

        session(session -> {
            Transaction transaction = session.beginTransaction();
            session.merge(record);
            transaction.commit();
        });

        return record;

    }

    protected void delete(@NotNull T record) {

        Objects.requireNonNull(record, "record == null");

        session(session -> {
            Transaction transaction = session.beginTransaction();
            session.remove(record);
            transaction.commit();
        });

    }


    public @NotNull Class<T> getDataRecordClass() {
        return dataRecordClass;
    }

}
