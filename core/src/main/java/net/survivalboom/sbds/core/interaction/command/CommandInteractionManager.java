package net.survivalboom.sbds.core.interaction.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.SBDS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class CommandInteractionManager extends Manager {

    private static final Logger log = LoggerFactory.getLogger(CommandInteractionManager.class);

    private final SBDS sbds;


    private final List<Supplier<List<CommandData>>> guildCommandUpdates = new ArrayList<>();

    private final List<Supplier<List<CommandData>>> globalCommandUpdates = new ArrayList<>();


    public CommandInteractionManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
    }


    @Override
    protected void init0() {

    }

    @Override
    protected void shutdown0() {
        guildCommandUpdates.clear();
        globalCommandUpdates.clear();
    }

    public void putGuild(@NotNull Supplier<List<CommandData>> supplier) {
        guildCommandUpdates.add(supplier);
    }

    public void putGlobal(@NotNull Supplier<List<CommandData>> supplier) {
        globalCommandUpdates.add(supplier);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void update() {

        checkValid();

        List<CommandData> guildCommands = guildCommandUpdates.stream().map(this::getCommandData).filter(Objects::nonNull).flatMap(List::stream).toList();
        List<CommandData> globalCommands = globalCommandUpdates.stream().map(this::getCommandData).filter(Objects::nonNull).flatMap(List::stream).toList();

        JDA jda = sbds.getBot();

        CommandListUpdateAction action = jda.updateCommands();
        globalCommands.forEach(action::addCommands);
        action.queue();

        jda.getGuilds().forEach(guild -> {
            CommandListUpdateAction a = guild.updateCommands();
            guildCommands.forEach(a::addCommands);
            a.queue();
        });

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void update(@NotNull Guild guild) {

        Objects.requireNonNull(guild, "guild == null");

        List<CommandData> guildCommands = guildCommandUpdates.stream().map(this::getCommandData).filter(Objects::nonNull).flatMap(List::stream).toList();

        var action = guild.updateCommands();
        guildCommands.forEach(action::addCommands);

        action.queue();

    }

    private @Nullable List<CommandData> getCommandData(@NotNull Supplier<List<CommandData>> supplier) {

        try {
            return supplier.get();
        }

        catch (Throwable t) {
            log.error("An error occurred in command update processor.", t);
        }

        return null;

    }

}
