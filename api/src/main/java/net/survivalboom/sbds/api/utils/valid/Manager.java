package net.survivalboom.sbds.api.utils.valid;

public abstract class Manager extends Valid implements IManager {

    public Manager() {
        setValid(false);
    }

    public void init() {
        if (isValid()) throw new IllegalStateException("Manager already initialized");
        setValid(true);
        init0();
    }

    public void shutdown() {
        checkValid();
        shutdown0();
        setValid(false);
    }

    public void shutdownIfNeeded() {
        if (!isValid()) return;
        shutdown();
    }


    protected abstract void init0();

    protected abstract void shutdown0();

}
