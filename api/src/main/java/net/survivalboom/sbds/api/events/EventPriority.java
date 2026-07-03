package net.survivalboom.sbds.api.events;

public enum EventPriority {

    MONITOR(-3),
    LOW(-2),
    LOWER(-1),
    NORMAL(0),
    HIGH(1),
    HIGHEST(2)

    ;

    private final int priority;

    EventPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }


}
