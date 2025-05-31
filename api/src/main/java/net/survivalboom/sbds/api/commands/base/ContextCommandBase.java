package net.survivalboom.sbds.api.commands.base;

import net.survivalboom.sbds.api.commands.context.ContextCommand;
import net.survivalboom.sbds.api.commands.context.ContextCommandExecutor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

// TODO Додати метод init();
public abstract class ContextCommandBase implements ContextCommandExecutor {

    private final String name;


    private final String permission;

    private final boolean defaultPermission;


    private final boolean global;

    private final boolean guild;


    public ContextCommandBase() {

        Command info = getInfoAnnotation();

        this.name = info.name();

        this.permission = info.permission().isEmpty() ? null : info.permission();
        this.defaultPermission = info.defaultPermission();

        this.global = info.global();
        this.guild = info.guild();

        Objects.requireNonNull(name, "name == null");

    }

    public @NotNull ContextCommand build() {

        ContextCommand.Builder builder = ContextCommand.builder(name, type());

        return builder
                .setGlobal(global)
                .setGuild(guild)
                .setDefaultPermission(defaultPermission)
                .setPermission(permission)
                .setExecutor(this)
                .build();

    }

    private @NotNull Command getInfoAnnotation() {
        Command annotation = this.getClass().getAnnotation(Command.class);
        if (annotation == null) throw new IllegalStateException("Annotation @Command is not present!");
        return annotation;
    }

}
