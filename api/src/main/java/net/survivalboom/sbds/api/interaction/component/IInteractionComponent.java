package net.survivalboom.sbds.api.interaction.component;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

// TODO: За допомогою цього інтерфейсу спробувати виправити проблеми
public interface IInteractionComponent {

    @NotNull String getName();

    int getRow();

    int getPriority();

    boolean isStatic();

    @NotNull Component.Type getType();

    @NotNull Component createComponent(@NotNull Function<String, String> parser, @Nullable Function<IInteractionComponent, String> componentIdCreator);

    // TODO Винести в окремий клас щось типу ModalComponent.
    @NotNull ModalTopLevelComponent createModalComponent(@NotNull Function<String, String> parser);

}
