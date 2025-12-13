package net.survivalboom.sbds.api.interaction.component.dropdown.entity;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.survivalboom.sbds.api.interaction.component.IComponent;
import net.survivalboom.sbds.api.interaction.component.dropdown.AbstractDropdownComponent;
import net.survivalboom.sbds.api.messages.InvalidComponentException;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class EntityDropdownTemplate extends AbstractDropdownComponent<EntityDropdownTemplate.Builder, EntityDropdownTemplate, EntitySelectMenu> {

    protected final EntitySelectMenu.SelectTarget target;

    private EntityDropdownTemplate(
            @NotNull String name,
            @Nullable String title,
            @Nullable String placeholder,
            int minCount,
            int maxCount,
            int row,
            int priority,
            boolean isStatic,
            @NotNull EntitySelectMenu.SelectTarget target,
            @NotNull Component.Type type
    ) {
        super(name, title, placeholder, minCount, maxCount, row, priority, isStatic, type);
        this.target = target;
    }

    @Override
    public @NotNull EntitySelectMenu createComponent(@NotNull Function<String, String> parser, @Nullable Function<IComponent, String> componentIdCreator) {

        String id = componentIdCreator != null ? componentIdCreator.apply(this) : name;
        String description = this.description != null ? parser.apply(this.description) : null;

        return EntitySelectMenu.create(id, target)
                .setMaxValues(maxCount)
                .setMinValues(minCount)
                .setPlaceholder(description)
                .build();

    }

    @Override
    public @NotNull EntityDropdownTemplate.Builder copy() {
        return new Builder(this);
    }


    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static @NotNull EntityDropdownTemplate.Builder fromSection(@NotNull TypeMap map) throws InvalidComponentException {

        String targetRaw = map.get("target", String.class);
        EntitySelectMenu.SelectTarget target = CommonUtils.getEnumValue(EntitySelectMenu.SelectTarget.class, targetRaw);
        if (target == null) {
            throw new InvalidComponentException("Invalid select target `" + targetRaw + "`");
        }

        var builder = builder();
        AbstractDropdownComponent.fromSection(builder, map);

        return builder
                .setTarget(target);

    }

    public static class Builder extends AbstractDropdownComponent.Builder<Builder, EntityDropdownTemplate, EntitySelectMenu> {


        private EntitySelectMenu.SelectTarget target;


        private Builder() {}

        private Builder(@NotNull Builder builder) {
            super(builder);
            this.target = builder.target;
        }

        private Builder(@NotNull EntityDropdownTemplate template) {
            super(template);
            this.target = template.target;
        }


        public @NotNull Builder setTarget(@NotNull EntitySelectMenu.SelectTarget target) {
            this.target = target;
            return this;
        }

        public @Nullable EntitySelectMenu.SelectTarget getTarget() {
            return target;
        }

        //
        // BUILD
        //

        public @NotNull EntityDropdownTemplate build() {

            var type = switch (target) {
                case USER -> Component.Type.USER_SELECT;
                case ROLE -> Component.Type.ROLE_SELECT;
                case CHANNEL -> Component.Type.CHANNEL_SELECT;
            };

            return new EntityDropdownTemplate(name, title, description, minCount, maxCount, row, priority, isStatic, target, type);

        }

        @Override
        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
