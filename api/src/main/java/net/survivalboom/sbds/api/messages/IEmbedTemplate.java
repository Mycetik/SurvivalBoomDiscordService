package net.survivalboom.sbds.api.messages;

import net.dv8tion.jda.api.EmbedBuilder;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface IEmbedTemplate {

    @NotNull Map<String, String> dump();

    @NotNull EmbedBuilder build(@Nullable Placeholders pl);

}
