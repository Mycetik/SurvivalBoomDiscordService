package net.survivalboom.sbds.api.interaction.dropdown.entity;

import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.survivalboom.sbds.api.messages.Component;
import net.survivalboom.sbds.api.messages.InvalidComponentException;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public class EntityDropdownTemplate implements Component {

    private final String name;

    private final int minCount;

    private final int maxCount;

    private final int row;

    private final int priority;

    private final boolean isStatic;

    private final String placeholder;


    private final EntitySelectMenu.SelectTarget target;

    private final net.dv8tion.jda.api.interactions.components.Component.@NotNull Type type;


    private EntityDropdownTemplate(
            @Nullable String name,
            int minCount,
            int maxCount,
            int row,
            int priority,
            boolean isStatic,
            @Nullable String placeholder,
            @NotNull EntitySelectMenu.SelectTarget target
        ) {
        this.name = name;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.row = row;
        this.priority = priority;
        this.isStatic = isStatic;
        this.placeholder = placeholder;
        this.target = target;

        this.type = switch (target) {
            case USER -> net.dv8tion.jda.api.interactions.components.Component.Type.USER_SELECT;
            case ROLE -> net.dv8tion.jda.api.interactions.components.Component.Type.ROLE_SELECT;
            case CHANNEL -> net.dv8tion.jda.api.interactions.components.Component.Type.CHANNEL_SELECT;
        };

    }

    @Override
    public @NotNull ItemComponent build(@NotNull Function<Component, String> componentIdCreator, @NotNull Function<String, String> parser) {

        return EntitySelectMenu.create(componentIdCreator.apply(this), target)
                .setMaxValues(maxCount)
                .setMinValues(minCount)
                .setPlaceholder(placeholder)
                .build();

    }

    @Override
    public int row() {
        return row;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public @Nullable String name() {
        return name;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public net.dv8tion.jda.api.interactions.components.Component.@NotNull Type type() {
        return type;
    }


    public static class Builder {

        private String name;

        private int row;

        private int priority;

        private int minCount;

        private int maxCount;

        private boolean isStatic;

        private String placeholder;

        private EntitySelectMenu.SelectTarget target;


        private Builder(
                @Nullable String name,
                int minCount,
                int maxCount,
                int row,
                int priority,
                boolean isStatic,
                @Nullable String placeholder,
                @Nullable EntitySelectMenu.SelectTarget target
        ) {

            this.name = name;
            this.minCount = minCount;
            this.maxCount = maxCount;
            this.row = row;
            this.priority = priority;
            this.isStatic = isStatic;
            this.placeholder = placeholder;
            this.target = target;

        }

        public @NotNull Builder setTarget(@NotNull EntitySelectMenu.SelectTarget target) {
            this.target = target;
            return this;
        }

        public @NotNull Builder setName(@Nullable String name) {
            this.name = name;
            return this;
        }

        public @NotNull Builder setMinCount(int v) {
            this.minCount = v;
            return this;
        }

        public @NotNull Builder setMaxCount(int v) {
            this.maxCount = v;
            return this;
        }

        public @NotNull Builder setRow(int row) {
            this.row = row;
            return this;
        }

        public @NotNull Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public @NotNull Builder setStatic(boolean isStatic) {
            this.isStatic = isStatic;
            return this;
        }

        public @NotNull Builder setPlaceholder(@Nullable String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public @NotNull EntityDropdownTemplate build() {
            Objects.requireNonNull(target, "target == null");
            return new EntityDropdownTemplate(name, minCount, maxCount, row, priority, isStatic, placeholder, target);
        }

    }

    public static @NotNull Builder builder() {
        return new Builder(null, 0, 0, 0, 0, false, null, null);
    }

    public static @NotNull EntityDropdownTemplate create(@NotNull TypeMap typeMap) throws InvalidComponentException {

        String name = typeMap.get("name", String.class);
        boolean isStatic = Boolean.TRUE.equals(typeMap.get("static", Boolean.class));

        int row = typeMap.get("row", 1);
        int priority = typeMap.get("priority", 1);

        int max = typeMap.getCastOrDefault("max", Integer.class, 1);
        int min = typeMap.getCastOrDefault("min", Integer.class, max);

        String placeholder = typeMap.get("placeholder", String.class);

        String targetRaw = typeMap.get("target", String.class);
        EntitySelectMenu.SelectTarget target = CommonUtils.getEnumValue(EntitySelectMenu.SelectTarget.class, targetRaw);
        if (target == null) throw new InvalidComponentException("Invalid select target `" + targetRaw + "`");

        Builder builder = builder()
                .setName(name)
                .setRow(row)
                .setPriority(priority)
                .setMinCount(min)
                .setMaxCount(max)
                .setStatic(isStatic)
                .setPlaceholder(placeholder)
                .setTarget(target);

        return builder.build();

    }

}
