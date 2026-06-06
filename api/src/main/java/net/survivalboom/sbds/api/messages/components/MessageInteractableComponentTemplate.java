package net.survivalboom.sbds.api.messages.components;

import net.dv8tion.jda.api.components.Component;
import net.survivalboom.sbds.api.messages.components.templates.ButtonTemplate;
import net.survivalboom.sbds.api.messages.components.templates.EntitySelectTemplate;
import net.survivalboom.sbds.api.messages.components.templates.StringSelectTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

public interface MessageInteractableComponentTemplate<T extends Component> extends ComponentTemplate<T> {

    @Nullable String getName();

    boolean isStatic();

    //
    // STATIC
    //

    static @NotNull MessageInteractableComponentTemplate<?> fromSection(@NotNull ConfigurationNode node) {

        String type = node.node("type").getString();
        if (type == null) {
            throw new IllegalArgumentException("No key `type` found");
        }

        return switch (type) {

            case "BUTTON" -> ButtonTemplate.fromSection(node).build();

            case "STRING_SELECT" -> StringSelectTemplate.fromSection(node).build();

            case "ENTITY_SELECT" -> EntitySelectTemplate.fromSection(node).build();

            default -> throw new IllegalArgumentException("Unknown type `" + type + "`");

        };

    }

}
