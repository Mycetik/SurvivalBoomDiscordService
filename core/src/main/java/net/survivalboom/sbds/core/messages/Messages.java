package net.survivalboom.sbds.core.messages;

import net.survivalboom.sbds.api.utils.Manager;

import java.util.HashMap;
import java.util.Map;

public class Messages extends Manager {

    private final Map<String, String> messages = new HashMap<>();

    @Override
    protected void init0() {

    }

    @Override
    protected void shutdown0() {
        messages.clear();
    }

}
