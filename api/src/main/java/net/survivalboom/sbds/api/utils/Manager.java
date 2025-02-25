package net.survivalboom.sbds.api.utils;

public abstract class Manager extends Valid {

    public Manager() {
        valid(false);
    }

    public void init() {
        if (valid()) throw new IllegalStateException("Manager already initialized");
        valid(true);
        init0();
    }

    public void shutdown() {
        checkValid();
        shutdown0();
        valid(false);
    }


    protected abstract void init0();

    protected abstract void shutdown0();

}
