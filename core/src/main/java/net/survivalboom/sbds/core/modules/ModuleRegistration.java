package net.survivalboom.sbds.core.modules;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ModuleRegistration {

    private final List<Reg> regList = new ArrayList<>();


    public void add(@NotNull String name, @NotNull Runnable unReg) {
        regList.add(new Reg(name, unReg));
    }

    public synchronized void unregister() {
        regList.forEach(reg -> reg.onUnregister().run());
        regList.clear();
    }

    public @NotNull List<Reg> regList() {
        return new ArrayList<>(regList);
    }

    public record Reg(@NotNull String name, @NotNull Runnable onUnregister) {}

}
