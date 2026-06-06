package net.survivalboom.sbds.core.commands.context;

import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.CommandExecutor;
import net.survivalboom.sbds.api.commands.context.*;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.EventListener;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.commands.AbstractCommandManager;
import net.survivalboom.sbds.core.interaction.command.CommandInteractionManager;
import org.jetbrains.annotations.NotNull;

public class ContextCommandManager extends AbstractCommandManager<IContextCommandManager.IRegisteredContextCommand, IContextCommandManager> implements IContextCommandManager, EventListener {

    private final CommandInteractionManager commandInteractionManager;

    public ContextCommandManager(@NotNull SBDS sbds) {
        super(sbds);
        this.commandInteractionManager = sbds.getCommandInteractionManager();
    }

    //
    // MANAGER
    //

    @Override
    protected void init0() {
        super.init0();
        sbds.getEventManager().registerEvents0(null, this);
    }

    @Override
    protected void shutdown0() {
        sbds.getEventManager().unregisterEvents(this);
        super.shutdown0();
    }

    @Override
    protected @NotNull IContextCommandManager.IRegisteredContextCommand createCommandReg(@NotNull Command command) {
        return new RegisteredContextCommand(this, command);
    }

    @Override
    public void onRegister(@NotNull Registration<IRegisteredContextCommand> registration) {

        Command command = registration.object().getCommand();
        CommandExecutor executor = command.getExecutor();

        if (executor == null) {
            return;
        }

        if (!(executor instanceof ContextCommandExecutor<?>)) {
            throw new IllegalArgumentException("Command `" + command.getName() + "` does not have executor for a context command");
        }

        commandInteractionManager.requestGlobalUpdate();

    }

    @Override
    public void unRegister(@NotNull Registration<IRegisteredContextCommand> registration) {
        commandInteractionManager.requestGlobalUpdate();
    }

    //
    // HANDLER
    //

    @EventHandler
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        onEvent(event);
    }

    @EventHandler
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        onEvent(event);
    }


    public void onEvent(@NotNull GenericContextInteractionEvent<?> event) {

        if (!sbds.isReady()) {
            return;
        }

        try {

            String name = event.getName();

            IRegisteredContextCommand registeredContextCommand = registry.getRegisteredObjects().stream()
                    .filter(c -> c.getCommand().getName().equals(name))
                    .findAny()
                    .orElse(null);

            if (registeredContextCommand == null) {
                logger.warn("Received unknown context command `{}` execution request.", name);
                return;
            }

            var executor = registeredContextCommand.getCommand().getExecutor();
            switch (event.getCommandType()) {

                case USER -> {

                    UserContextInteractionEvent event0 = (UserContextInteractionEvent) event;
                    UserContextCommandExecutor executor0 = (UserContextCommandExecutor) executor;
                    UserContextInteractionInfo info = new UserContextInteractionInfo(event0, registeredContextCommand, registeredContextCommand.getCommand(), name, sbds);

                    if (executor0 != null) {
                        executor0.execute(info);
                    }

                }

                case MESSAGE -> {

                    MessageContextInteractionEvent event0 = (MessageContextInteractionEvent) event;
                    MessageContextCommandExecutor executor0 = (MessageContextCommandExecutor) executor;
                    MessageContextInteractionInfo info = new MessageContextInteractionInfo(event0, registeredContextCommand, registeredContextCommand.getCommand(), name, sbds);

                    if (executor0 != null) {
                        executor0.execute(info);
                    }

                }

            }

        }

        catch (Throwable t) {
            logger.error("An internal error occurred while attempting to perform context command.", t);
            sbds.getMessages().reply(event, "common.error", event.getUser())
                    .withPlaceholders("{exception}", t)
                    .setEphemeral(true)
                    .queue();
        }

    }

    public static class RegisteredContextCommand extends RegisteredCommand<IRegisteredContextCommand, IContextCommandManager> implements IRegisteredContextCommand {

        public RegisteredContextCommand(@NotNull IContextCommandManager manager, @NotNull Command command) {
            super(manager, command);
        }

    }

}
