package net.survivalboom.sbds.api.commands.context;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContextCommand {

    private final String name;

    private final Command.Type type;


    private final String permission;

    private final boolean defaultPermission;


    private final boolean global;

    private final boolean guild;


    private final ContextCommandExecutor executor;


    public ContextCommand(@NotNull String name, @NotNull Command.Type type, @NotNull String permission, boolean defaultPermission, boolean global, boolean guild, @NotNull ContextCommandExecutor executor) {

        this.name = name;
        this.type = type;

        this.permission = permission;
        this.defaultPermission = defaultPermission;

        this.global = global;
        this.guild = guild;

        this.executor = executor;

    }


    public @NotNull CommandData build() {
        return Commands.context(type, name);
    }

    public @NotNull String name() {
        return name;
    }

    public @NotNull Command.Type type() {
        return type;
    }

    public @NotNull String permission() {
        return permission;
    }

    public boolean isDefaultPermission() {
        return defaultPermission;
    }


    public boolean global() {
        return global;
    }

    public boolean guild() {
        return guild;
    }

    public @NotNull ContextCommandExecutor executor() {
        return executor;
    }


    public static class Builder {

        private final String name;

        private final Command.Type type;


        private String permission;

        private boolean defaultPermission;


        private boolean global;

        private boolean guild;


        private ContextCommandExecutor executor;


        private Builder(
                @NotNull String name,
                @NotNull Command.Type type,
                @Nullable String permission,
                boolean defaultPermission,
                boolean global,
                boolean guild,
                @Nullable ContextCommandExecutor executor
        ) {

            this.name = name;
            this.type = type;

            this.permission = permission;
            this.defaultPermission = defaultPermission;

            this.global = global;
            this.guild = guild;

            this.executor = executor;

        }

        public @NotNull Builder setPermission(@Nullable String permission) {
            this.permission = permission;
            return this;
        }

        public @NotNull Builder setDefaultPermission(boolean v) {
            this.defaultPermission = v;
            return this;
        }

        public @NotNull Builder setGuild(boolean v) {
            this.guild = v;
            return this;
        }

        public @NotNull Builder setGlobal(boolean v) {
            this.global = v;
            return this;
        }

        public @NotNull Builder setUserExecutor(@NotNull UserContextCommand executor) {
            this.executor = executor;
            return this;
        }

        public @NotNull Builder setMessageExecutor(@NotNull MessageContextCommand executor) {
            this.executor = executor;
            return this;
        }

        public @NotNull Builder setExecutor(@NotNull ContextCommandExecutor executor) {
            this.executor = executor;
            return this;
        }

        public @NotNull ContextCommand build() {
            return new ContextCommand(name, type, permission, defaultPermission, global, guild, executor);
        }

    }


    public static @NotNull Builder builder(@NotNull String name, @NotNull Command.Type type) {
        return new Builder(name, type, null, false, false, false, null);
    }

}
