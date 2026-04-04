package net.survivalboom.sbds.api.messages.components;

import net.dv8tion.jda.api.components.Component;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.TypeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface ComponentTemplate {

    @NotNull Component build(@Nullable StringParser parser, @Nullable ComponentLinker linker);

    int row();

    int priority();

    @Nullable String name();

    boolean isStatic();

    @NotNull Component.Type type();


    static @NotNull List<ComponentTemplate> createComponents(@NotNull List<TypeMap> mapList) throws InvalidComponentException {

        List<ComponentTemplate> list = new ArrayList<>();
        for (TypeMap map : mapList) {
            ComponentTemplate component = createComponent(map);
            list.add(component);
        }

        return list;

    }

    static @NotNull ComponentTemplate createComponent(@NotNull TypeMap typeMap) throws InvalidComponentException {

        String typeRaw = typeMap.get("type", String.class);
        if (typeRaw == null) {
            throw new InvalidComponentException("Component does not have a type");
        }

        Component.Type type = CommonUtils.getEnumValue(Component.Type.class, typeRaw);
        if (type == null) {
            throw new InvalidComponentException("Invalid component type `" + typeRaw + "`");
        }

        return switch (type) {

            case ACTION_ROW -> null;

            case BUTTON -> null;

            case STRING_SELECT -> null;

            case TEXT_INPUT -> null;

            case USER_SELECT -> null;

            case ROLE_SELECT -> null;

            case MENTIONABLE_SELECT -> null;

            case CHANNEL_SELECT -> null;

            case SECTION -> null;

            case TEXT_DISPLAY -> null;

            case THUMBNAIL -> null;

            case MEDIA_GALLERY -> null;

            case FILE_DISPLAY -> null;

            case SEPARATOR -> null;

            case CONTAINER -> null;

            case LABEL -> null;

            case FILE_UPLOAD -> null;

            case RADIO_GROUP -> null;

            case CHECKBOX_GROUP -> null;

            case CHECKBOX -> null;

            default -> throw new RuntimeException("Unknown type `" + typeRaw + "`");

        };

    }

}
