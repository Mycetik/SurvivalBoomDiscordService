package net.survivalboom.sbds.api.messages;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.survivalboom.sbds.api.interaction.component.button.ButtonTemplate;
import net.survivalboom.sbds.api.interaction.component.dropdown.entity.EntityDropdownTemplate;
import net.survivalboom.sbds.api.interaction.component.dropdown.string.StringDropdownTemplate;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface MessageComponent {

    @NotNull ActionRowChildComponent build(@NotNull Function<MessageComponent, String> componentIdCreator, @NotNull Function<String, String> parser);

    int row();

    int priority();

    @Nullable String name();

    boolean isStatic();

    @NotNull Component.Type type();


    static @NotNull List<MessageComponent> createComponents(@NotNull List<TypeMap> mapList) throws InvalidComponentException {

        List<MessageComponent> list = new ArrayList<>();
        for (TypeMap map : mapList) {
            list.add(createComponent(map));
        }

        return list;

    }

    static @NotNull MessageComponent createComponent(@NotNull TypeMap typeMap) throws InvalidComponentException {

        String type = typeMap.get("type", String.class);
        if (type == null) throw new InvalidComponentException("Component does not have a type");

        return switch (type.toLowerCase()) {

            case "button" -> ButtonTemplate.fromSection(typeMap);

            case "string_select" -> StringDropdownTemplate.fromSection(typeMap);

            case "entity_select" -> EntityDropdownTemplate.fromSection(typeMap);

            default -> throw new InvalidComponentException("Invalid component type `" + type + "`");

        };

    }


}
