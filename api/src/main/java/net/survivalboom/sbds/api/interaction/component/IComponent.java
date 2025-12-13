package net.survivalboom.sbds.api.interaction.component;

import net.dv8tion.jda.api.components.Component;
import net.survivalboom.sbds.api.interaction.component.button.ButtonTemplate;
import net.survivalboom.sbds.api.interaction.component.dropdown.entity.EntityDropdownTemplate;
import net.survivalboom.sbds.api.interaction.component.dropdown.string.StringDropdownTemplate;
import net.survivalboom.sbds.api.messages.InvalidComponentException;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface IComponent {

    @NotNull String getName();

    int getRow();

    int getPriority();

    boolean isStatic();

    @NotNull Component.Type getType();

    @NotNull Component createComponent(@NotNull Function<String, String> parser, @Nullable Function<IComponent, String> componentIdCreator);


    static @NotNull List<IComponent> createComponents(@NotNull List<TypeMap> mapList) throws InvalidComponentException {

        List<IComponent> list = new ArrayList<>();
        for (TypeMap map : mapList) {
            list.add(createComponent(map));
        }

        return list;

    }

    static @NotNull IComponent createComponent(@NotNull TypeMap typeMap) throws InvalidComponentException {

        String type = typeMap.get("type", String.class);
        if (type == null) throw new InvalidComponentException("Component does not have a type");

        return switch (type.toLowerCase()) {

            case "button" -> ButtonTemplate.fromSection(typeMap).build();

            case "string_select" -> StringDropdownTemplate.fromSection(typeMap).build();

            case "entity_select" -> EntityDropdownTemplate.fromSection(typeMap).build();

            default -> throw new InvalidComponentException("Invalid component type `" + type + "`");

        };

    }


}
