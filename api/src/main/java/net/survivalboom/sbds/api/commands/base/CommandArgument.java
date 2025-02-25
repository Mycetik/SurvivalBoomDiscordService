package net.survivalboom.sbds.api.commands.base;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandArgument {
    @NotNull String name();
    @NotNull String description() default "";
    int index() default 0;
    boolean required() default true;
}
