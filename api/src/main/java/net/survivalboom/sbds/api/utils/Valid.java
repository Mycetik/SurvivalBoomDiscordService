package net.survivalboom.sbds.api.utils;

public abstract class Valid {

    private boolean valid = true;

    protected void checkValid() {
        if (!valid) throw new IllegalStateException("Object is no longer valid");
    }

    protected void valid(boolean v) {
        this.valid = v;
    }

    public boolean valid() {
        return valid;
    }

}
