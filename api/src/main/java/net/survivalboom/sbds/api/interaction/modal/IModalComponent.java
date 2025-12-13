package net.survivalboom.sbds.api.interaction.modal;

import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.survivalboom.sbds.api.interaction.component.IComponent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface IModalComponent extends IComponent {

    @NotNull ModalTopLevelComponent createModalComponent(@NotNull Function<String, String> parser);

}
