package net.survivalboom.sbds.api.commands.base;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    @NotNull String name();
    @NotNull String[] aliases() default {};

    @NotNull String usage() default "";
    @NotNull String description() default "";

    @NotNull String permission() default "";
    boolean defaultPermission() default false;

}
