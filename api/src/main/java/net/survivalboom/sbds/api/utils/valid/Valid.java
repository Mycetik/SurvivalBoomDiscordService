package net.survivalboom.sbds.api.utils.valid;

public abstract class Valid implements IValid {

    private boolean valid = true;

    protected void checkValid() {
        if (!valid) {
            throw new IllegalStateException("Object `" + this + "` is no longer valid");
        }
    }

    protected void setValid(boolean v) {
        this.valid = v;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

}
