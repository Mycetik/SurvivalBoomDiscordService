package net.survivalboom.sbds.api.events;

public interface ICancellable {

    boolean isCancelled();

    void setCancelled(boolean cancelled);

}
