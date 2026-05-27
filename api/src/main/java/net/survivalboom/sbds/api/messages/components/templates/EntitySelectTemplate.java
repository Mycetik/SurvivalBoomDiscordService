package net.survivalboom.sbds.api.messages.components.templates;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.survivalboom.sbds.api.messages.components.MessageInteractableComponentTemplate;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.utils.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Objects;

public class EntitySelectTemplate implements MessageInteractableComponentTemplate<EntitySelectMenu> {

    private final String name;

    private final int minCount;

    private final int maxCount;

    private final int row;

    private final boolean isStatic;

    private final @Nullable String placeholder;

    private final EntitySelectMenu.SelectTarget target;

    private final Component.Type type;


    public EntitySelectTemplate(
            @Nullable String name,
            int minCount,
            int maxCount,
            int row,
            boolean isStatic,
            @Nullable String placeholder,
            @NotNull EntitySelectMenu.SelectTarget target
    ) {

        Objects.requireNonNull(target, "target == null");

        this.name = name;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.row = row;
        this.isStatic = isStatic;
        this.placeholder = placeholder;
        this.target = target;

        this.type = switch (target) {
            case USER -> Component.Type.USER_SELECT;
            case ROLE -> Component.Type.ROLE_SELECT;
            case CHANNEL -> Component.Type.CHANNEL_SELECT;
        };

    }

    @Override
    public int getRow() {
        return row;
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    // COMPONENT //


    @Override
    public @NotNull Class<EntitySelectMenu> getComponentClass() {
        return EntitySelectMenu.class;
    }

    @Override
    public @NotNull Component.Type getType() {
        return type;
    }

    @Override
    public @NotNull EntitySelectMenu build(@Nullable StringParser parser, @Nullable ComponentLinker linker) {
        return EntitySelectMenu.create(ComponentLinker.stLink(linker, this), target)
                .setMaxValues(maxCount)
                .setMinValues(minCount)
                .setPlaceholder(placeholder)
                .build();
    }

    public @NotNull Builder copy() {
        return new Builder(this);
    }

    //
    // BUILDER
    //

    public static @NotNull Builder fromSection(@NotNull ConfigurationNode section) {

        String name = section.node("name").getString();

        int min = section.node("min").getInt();
        int max = section.node("max").getInt();

        int row = section.node("row").getInt();
        boolean isStatic = section.node("static").getBoolean();

        String placeholder = section.node("placeholder").getString();

        String targetRaw = section.node("target").getString();
        EntitySelectMenu.SelectTarget target = CommonUtils.getEnumValue(EntitySelectMenu.SelectTarget.class, targetRaw);

        return builder()
                .setName(name)
                .setMinCount(min)
                .setMaxCount(max)
                .setRow(row)
                .setStatic(isStatic)
                .setPlaceholder(placeholder)
                .setTarget(target);

    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private int row;

        private int minCount;

        private int maxCount;

        private boolean isStatic;

        private String placeholder;

        private EntitySelectMenu.SelectTarget target;


        private Builder(Builder builder) {

            this.name = builder.name;
            this.minCount = builder.minCount;
            this.maxCount = builder.maxCount;
            this.row = builder.row;
            this.isStatic = builder.isStatic;
            this.placeholder = builder.placeholder;
            this.target = builder.target;

        }

        private Builder(EntitySelectTemplate template) {

            this.name = template.name;
            this.minCount = template.minCount;
            this.maxCount = template.maxCount;
            this.row = template.row;
            this.isStatic = template.isStatic;
            this.placeholder = template.placeholder;
            this.target = template.target;

        }

        private Builder() {}

        // TARGET //

        public @NotNull Builder setTarget(@NotNull EntitySelectMenu.SelectTarget target) {
            this.target = target;
            return this;
        }

        public EntitySelectMenu.SelectTarget getTarget() {
            return target;
        }

        // NAME //

        public @NotNull Builder setName(@Nullable String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return name;
        }

        // min //

        public @NotNull Builder setMinCount(int v) {
            this.minCount = v;
            return this;
        }

        public int getMinCount() {
            return minCount;
        }

        // max //

        public @NotNull Builder setMaxCount(int v) {
            this.maxCount = v;
            return this;
        }

        public int getMaxCount() {
            return maxCount;
        }

        // row //

        public @NotNull Builder setRow(int row) {
            this.row = row;
            return this;
        }

        public int getRow() {
            return row;
        }

        // static //

        public @NotNull Builder setStatic(boolean isStatic) {
            this.isStatic = isStatic;
            return this;
        }

        public boolean isStatic() {
            return isStatic;
        }

        // placeholder //

        public @NotNull Builder setPlaceholder(@Nullable String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public String getPlaceholder() {
            return placeholder;
        }

        // BUILD //

        public @NotNull EntitySelectTemplate build() {
            return new EntitySelectTemplate(name, minCount, maxCount, row, isStatic, placeholder, target);
        }

    }

}
