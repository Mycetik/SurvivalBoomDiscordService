package net.survivalboom.sbds.api.interaction.component;

import net.dv8tion.jda.api.components.Component;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractInteractionComponent<
        B extends AbstractInteractionComponent.Builder<B, C, CO>,
        C extends AbstractInteractionComponent<B, C, CO>,
        CO extends Component
> {

    protected final String name;


    protected final int row;

    protected final int priority;

    protected final boolean isStatic;


    protected final Component.Type type;


    public AbstractInteractionComponent(
            @NotNull String name,
            int row,
            int priority,
            boolean isStatic,
            Component.Type type
    ) {

        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(type, "type == null");

        this.name = name;

        this.row = row;
        this.priority = priority;
        this.isStatic = isStatic;
        this.type = type;

    }

    public @NotNull String getName() {
        return name;
    }

    public int getRow() {
        return row;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public @NotNull Component.Type getType() {
        return type;
    }


    public abstract @NotNull B copy();

    public abstract @NotNull CO createComponent(@NotNull Function<String, String> parser, @Nullable Function<C, String> componentIdCreator);


    //
    // BUILDER
    //

    public static <B extends Builder<B, C, CO>, C extends AbstractInteractionComponent<B, C, CO>, CO extends Component> @NotNull Builder<B, C, CO> fromSection(@NotNull Builder<B, C, CO> builder, @NotNull TypeMap map) {

        String name = map.getCastNotNull("name", String.class);
        boolean isStatic = map.getCastOrDefault("static", Boolean.class, false);

        int row = map.getCastOrDefault("row", Integer.class, 0);
        int priority = map.getCastOrDefault("priority", Integer.class, 0);

        return builder
                .setName(name)
                .setStatic(isStatic)
                .setRow(row)
                .setPriority(priority);

    }

    public static abstract class Builder<
            B extends Builder<B, C, CO>,
            C extends AbstractInteractionComponent<B, C, CO>,
            CO extends Component
    > {

        protected String name;

        protected int row = 0;

        protected int priority = 0;

        protected boolean isStatic = false;


        protected Builder() {}

        protected Builder(@NotNull Builder<B, C, CO> builder) {
            this.name = builder.name;
            this.row = builder.row;
            this.priority = builder.priority;
            this.isStatic = builder.isStatic;
        }

        protected Builder(@NotNull AbstractInteractionComponent<B, C, CO> component) {
            this.name = component.name;
            this.row = component.row;
            this.priority = component.priority;
            this.isStatic = component.isStatic;
        }

        // NAME //

        public @NotNull B setName(@NotNull String name) {
            this.name = name;
            return This();
        }

        public @Nullable String getName() {
            return name;
        }

        // ROW //

        public @NotNull B setRow(int row) {
            this.row = row;
            return This();
        }

        public int getRow() {
            return row;
        }

        // PRIORITY //

        public @NotNull B setPriority(int priority) {
            this.priority = priority;
            return This();
        }

        public int getPriority() {
            return priority;
        }

        // STATIC //

        public @NotNull B setStatic(boolean v) {
            this.isStatic = v;
            return This();
        }

        public boolean isStatic() {
            return isStatic;
        }

        //
        // BUILD
        //

        public abstract @NotNull C build();

        public abstract @NotNull B copy();

        // abstract //

        @SuppressWarnings("unchecked")
        public @NotNull B This() {
            return (B) this;
        }

    }

}
