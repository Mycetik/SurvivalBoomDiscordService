package net.survivalboom.sbds.core.commands.context;

import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.survivalboom.sbds.api.commands.context.ContextCommand;
import net.survivalboom.sbds.api.commands.context.ContextCommandExecutor;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.commands.context.ContextInteractionInfo;
import net.survivalboom.sbds.api.commands.context.IContextCommandManager;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.interaction.command.CommandInteractionManager;
import net.survivalboom.sbds.core.modules.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ContextCommandManager extends Manager implements Listener, IContextCommandManager {

    private static final Logger log = LoggerFactory.getLogger(ContextCommandManager.class.getSimpleName());

    private final SBDS sbds;

    private final CommandInteractionManager commandInteractionManager;

    private final Set<RegisteredContextCommand> registeredContextCommands = new HashSet<>();

    public ContextCommandManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.commandInteractionManager = sbds.getCommandInteractionManager();
    }

    @Override
    protected void init0() {

        sbds.getEventManager().registerEvents0(null, this);

        commandInteractionManager.putGuild(this::prepareGuild);
        commandInteractionManager.putGlobal(this::prepareGlobal);

    }

    @Override
    protected void shutdown0() {
        sbds.getEventManager().unregisterEvents(this);
    }


    private List<CommandData> prepareGlobal() {
        return registeredContextCommands.stream().filter(c -> c.command.global()).map(c -> c.command.build()).toList();
    }

    private List<CommandData> prepareGuild() {
        return registeredContextCommands.stream().filter(c -> c.command.guild()).map(c -> c.command.build()).toList();
    }



    @Override
    public @NotNull ContextCommandManager.RegisteredContextCommand registerContextCommand(@NotNull IModule module, @NotNull ContextCommand command) {

        Objects.requireNonNull(module, "module == null");

        return registerContextCommand0(module, command);

    }

    public @NotNull ContextCommandManager.RegisteredContextCommand registerContextCommand0(@Nullable IModule imodule, @NotNull ContextCommand command) {

        String name = command.name();
        Module module = imodule != null ? sbds.getModuleManager().checkModuleEnabled(imodule, "Disabled module tried to register a context command") : null;

        if (registeredContextCommands.stream().anyMatch(c -> c.command.name().equals(name))) {
            throw new IllegalArgumentException("Command with name `" + name + "` already exists");
        }

        RegisteredContextCommand registeredContextCommand = new RegisteredContextCommand(module, command);

        registeredContextCommands.add(registeredContextCommand);
        commandInteractionManager.update();

        if (module != null) {
            module.getRegistration().add("ContextCommand-" + name, () -> unregisterContextCommand(name));
        }

        return registeredContextCommand;

    }

    @Override
    public void unregisterContextCommand(@NotNull String name) {
        registeredContextCommands.removeIf(c -> c.command.name().equals(name));
    }


    @EventHandler
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        onEvent(event);
    }

    @EventHandler
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        onEvent(event);
    }


    public void onEvent(@NotNull GenericContextInteractionEvent<?> event) {

        if (!sbds.isReady()) return;

        try {

            String name = event.getName();

            RegisteredContextCommand registeredContextCommand = registeredContextCommands.stream()
                    .filter(c -> c.command.name().equals(name) && c.command.type().equals(event.getCommandType()))
                    .findAny().orElse(null);

            if (registeredContextCommand == null) return;

            ContextCommandExecutor executor = registeredContextCommand.command.executor();
            ContextInteractionInfo<?> info = executor.createInfo(event, sbds, log);
            executor.execute(info);

        }

        catch (Throwable t) {
            log.error("An internal error occurred while attempting to perform context command.", t);
            sbds.getMessages().reply(event, "common.error", event.getUser()).withPlaceholders(Placeholders.of("{EXCEPTION}", t)).send().setEphemeral(true).queue();
        }

    }


    public record RegisteredContextCommand(@Nullable IModule module, @NotNull ContextCommand command) implements IContextCommandManager.RegisteredContextCommand {}

}
