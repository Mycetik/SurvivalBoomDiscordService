package net.survivalboom.sbds.api.interaction.component.dropdown;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.survivalboom.sbds.api.interaction.component.AbstractLabelComponent;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractDropdownComponent<
        B extends AbstractDropdownComponent.Builder<B, C, CO>,
        C extends AbstractDropdownComponent<B, C, CO>,
        CO extends SelectMenu
> extends AbstractLabelComponent<B, C, CO> {

    protected final int minCount;

    protected final int maxCount;

    public AbstractDropdownComponent(
            @NotNull String name,
            @Nullable String title,
            @Nullable String placeholder,
            int minCount,
            int maxCount,
            int row,
            int priority,
            boolean isStatic,
            Component.@NotNull Type type
    ) {
        super(name, title, placeholder, row, priority, isStatic, type);

        this.minCount = minCount;
        this.maxCount = maxCount;

    }

    public int getMinCount() {
        return minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    //
    // BUILDER
    //

    public static <B extends AbstractDropdownComponent.Builder<B, C, CO>, C extends AbstractDropdownComponent<B, C, CO>, CO extends SelectMenu> AbstractDropdownComponent.@NotNull Builder<B, C, CO> fromSection(@NotNull AbstractDropdownComponent.Builder<B, C, CO> builder, @NotNull TypeMap map) {

        AbstractLabelComponent.fromSection(builder, map);

        int max = map.getCastOrDefault("max", Integer.class, 1);
        int min = map.getCastOrDefault("min", Integer.class, max);

        return builder
                .setMaxCount(max)
                .setMinCount(min);

    }

    public static abstract class Builder<
            B extends AbstractDropdownComponent.Builder<B, C, CO>,
            C extends AbstractDropdownComponent<B, C, CO>,
            CO extends SelectMenu
    > extends AbstractLabelComponent.Builder<B, C, CO> {

        protected int minCount = 1;

        protected int maxCount = 1;


        protected Builder() {}

        protected Builder(@NotNull Builder<B, C, CO> builder) {

            super(builder);

            this.minCount = builder.minCount;
            this.maxCount = builder.maxCount;

        }

        protected Builder(@NotNull AbstractDropdownComponent<B, C, CO> component) {

            super(component);

            this.minCount = component.minCount;
            this.maxCount = component.maxCount;

        }

        // MIN COUNT //

        public B setMinCount(int v) {
            this.minCount = v;
            return This();
        }

        public int getMinCount() {
            return minCount;
        }

        // MAX COUNT //

        public B setMaxCount(int v) {
            this.maxCount = v;
            return This();
        }

        public int getMaxCount() {
            return maxCount;
        }

    }

}
