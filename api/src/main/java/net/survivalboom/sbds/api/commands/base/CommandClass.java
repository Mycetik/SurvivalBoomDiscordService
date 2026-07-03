package net.survivalboom.sbds.api.commands.base;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandClass {

    @NotNull String name();
    @NotNull String[] aliases() default {};

    @NotNull String usage() default "";
    @NotNull String description() default "";
    @NotNull String translationKey() default "";

    @NotNull String permission() default "";
    boolean defaultPermission() default false;

    boolean global() default false;
    boolean guild() default true;

    boolean deferReply() default true;
    boolean ephemeral() default false;

}
