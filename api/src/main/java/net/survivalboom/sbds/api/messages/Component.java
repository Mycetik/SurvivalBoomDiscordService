package net.survivalboom.sbds.api.messages;

import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.survivalboom.sbds.api.interaction.button.ButtonTemplate;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface Component {

    @NotNull ItemComponent build(@NotNull Function<Component, String> componentIdCreator, @NotNull Function<String, String> parser);

    int row();

    int priority();

    @Nullable String name();

    boolean isStatic();


    static @NotNull List<Component> createComponents(@NotNull List<TypeMap> mapList) throws InvalidComponentException {

        List<Component> list = new ArrayList<>();
        for (TypeMap map : mapList) {
            list.add(createComponent(map));
        }

        return list;

    }

    static @NotNull Component createComponent(@NotNull TypeMap typeMap) throws InvalidComponentException {

        String type = typeMap.get("type", String.class);
        if (type == null) throw new InvalidComponentException("Component does not have a type");

        return switch (type.toLowerCase()) {

            case "button" -> ButtonTemplate.create(typeMap);

            default -> throw new InvalidComponentException("Invalid component type `" + type + "`");

        };

    }


}
