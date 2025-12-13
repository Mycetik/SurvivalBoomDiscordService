package net.survivalboom.sbds.api.interaction.component;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.survivalboom.sbds.api.interaction.modal.IModalComponent;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractLabelComponent<
        B extends AbstractLabelComponent.Builder<B, C, CO>,
        C extends AbstractLabelComponent<B, C, CO>,
        CO extends LabelChildComponent
> extends AbstractInteractionComponent<B, C, CO> implements IModalComponent {

    protected final @Nullable String title;

    protected final @Nullable String description;


    public AbstractLabelComponent(
            @NotNull String name,
            @Nullable String title,
            @Nullable String description,
            int row,
            int priority,
            boolean isStatic,
            @NotNull Component.Type type
    ) {
        super(name, row, priority, isStatic, type);

        this.title = title;
        this.description = description;

    }

    public @Nullable String getTitle() {
        return title;
    }

    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public @NotNull Label createModalComponent(@NotNull Function<String, String> parser) {

        String title = parser.apply(Objects.requireNonNullElse(this.title, "null"));
        String description = this.description != null ? parser.apply(this.description) : null;

        return Label.of(title, description, createComponent(parser,  null));

    }

    //
    // BUILDER
    //

    public static <B extends Builder<B, C, CO>, C extends AbstractLabelComponent<B, C, CO>, CO extends LabelChildComponent> @NotNull Builder<B, C, CO> fromSection(@NotNull Builder<B, C, CO> builder, @NotNull TypeMap map) {

        AbstractInteractionComponent.fromSection(builder, map);

        String title = map.getCastOrNull("title", String.class);
        String placeholder = map.getCastOrNull("placeholder", String.class);

        return builder
                .setTitle(title)
                .setDescription(placeholder);

    }

    public static abstract class Builder<
            B extends Builder<B, C, CO>,
            C extends AbstractLabelComponent<B, C, CO>,
            CO extends LabelChildComponent
    > extends AbstractInteractionComponent.Builder<B, C, CO> {

        protected String title;

        protected String description;


        protected Builder() {}

        protected Builder(@NotNull Builder<B, C, CO> builder) {
            super(builder);
            this.title = builder.title;
            this.description = builder.description;
        }

        protected Builder(@NotNull AbstractLabelComponent<B, C, CO> modalComponent) {
            super(modalComponent);
            this.title = modalComponent.title;
            this.description = modalComponent.description;
        }

        // TITLE //

        public @NotNull B setTitle(@Nullable String title) {
            this.title = title;
            return This();
        }

        public @Nullable String getTitle() {
            return title;
        }

        // PLACEHOLDER //

        public @NotNull B setDescription(@Nullable String description) {
            this.description = description;
            return This();
        }

        public @Nullable String getDescription() {
            return description;
        }

    }

}
