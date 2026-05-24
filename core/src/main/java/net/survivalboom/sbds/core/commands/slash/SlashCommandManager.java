package net.survivalboom.sbds.core.commands.slash;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.survivalboom.sbds.api.commands.Command;
import net.survivalboom.sbds.api.commands.argument.misc.SubCommandArgument;
import net.survivalboom.sbds.api.commands.slash.ISlashCommandManager;
import net.survivalboom.sbds.api.commands.slash.SlashCommandExecutor;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.EventListener;
import net.survivalboom.sbds.api.permissions.Permission;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import net.survivalboom.sbds.core.BuildConstants;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.commands.AbstractCommandManager;
import net.survivalboom.sbds.core.commands.cmds.common.StatusCommand;
import net.survivalboom.sbds.core.commands.parser.SlashCommandParser;
import net.survivalboom.sbds.core.interaction.command.CommandInteractionManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SlashCommandManager extends AbstractCommandManager<SlashCommandManager.IRegisteredSlashCommand, ISlashCommandManager> implements ISlashCommandManager, EventListener {

    private final CommandInteractionManager commandInteractionManager;

    public SlashCommandManager(@NotNull SBDS sbds) {
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

        registerCommand0(null, new StatusCommand().build());

    }

    @Override
    protected void shutdown0() {
        sbds.getEventManager().unregisterEvents(this);
        super.shutdown0();
    }

    @Override
    protected @NotNull SlashCommandManager.IRegisteredSlashCommand createCommandReg(@NotNull Command command) {
        return new RegisteredSlashCommand(this, command);
    }

    @Override
    public void onRegister(@NotNull Registration<IRegisteredSlashCommand> registration) {
        commandInteractionManager.registerCommand(registration.object(), net.dv8tion.jda.api.interactions.commands.Command.Type.SLASH);
    }

    @Override
    public void unRegister(@NotNull Registration<IRegisteredSlashCommand> registration) {
        commandInteractionManager.unregisterCommand(registration.object());
    }

    //
    // HANDLER
    //

    @EventHandler
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        commandInteractionManager.updateGuild(event.getGuild());
    }

    @EventHandler
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {

        if (!sbds.isReady()) {
            return;
        }

        String commandName = event.getName();

        IRegisteredSlashCommand registeredCommand = getByAlias(commandName);
        if (registeredCommand == null) {
            logger.warn("Something went wrong! Slash command with name `{}` does not exist in SlashCommandManager!", commandName);
            return;
        }

        Command baseCommand = registeredCommand.getCommand();

        try {

            Command command = getCommand(baseCommand, event);
            TypeMap arguments = SlashCommandParser.parse(registeredCommand, command, event);

            if (!permissionCheck(command, event)) {
                return;
            }

            SlashExecutionInfo info = new SlashExecutionInfo(event, registeredCommand, command, commandName, arguments);

            SlashCommandExecutor executor = (SlashCommandExecutor) command.getExecutor();
            executor.executes(info);

            if (!event.isAcknowledged()) {
                event.reply("Something went wrong. Looks like the executor `" + executor + "` refused to respond to the interaction.").queue();
                logger.error("Command executor of command /`{}` did not respond to the interaction. Are you sure you did it right?", commandName);
            }

        }

        catch (Throwable t) {

            logger.error("[{}] An internal error occurred while attempting to perform slash command /{}", event.getGuild() != null ? event.getGuild().getName() + ":" + event.getUser().getName() : event.getUser().getName(), commandName, t);

            try {

                if (!event.isAcknowledged()) {
                    messages.reply(event, "sbds.error", event.getUser())
                            .withPlaceholders(Placeholders.of("{exception}", t.toString()))
                            .queue();
                } else {
                    messages.createActionMessage("sbds.error", event.getUser(), d -> event.getHook().editOriginal(MessageEditData.fromCreateData(d)))
                            .withPlaceholders(Placeholders.of("{exception}", t.toString()))
                            .queue();
                }
            }

            catch (Exception e) {
                event.reply(
                    """ 
                    **SurvivalBoom Discord Service** *v{v}*
                    A low-level fatal error occurred in SurvivalBoom Discord Service while attempting to process your request!
                    This is an internal error. Looks like something went wrong completely wrong!
                    `{e}`
                    """.replace("{v}", BuildConstants.VERSION).replace("{e}", e.toString())
                ).queue();
                throw e;
            }

        }

    }

    private @NotNull Command getCommand(@NotNull Command baseCommand, @NotNull SlashCommandInteractionEvent event) {

        boolean hasSubCommands = baseCommand.getArguments().stream()
                .anyMatch(argument -> argument.argument() instanceof SubCommandArgument);

        if (!hasSubCommands) {
            return baseCommand;
        }

        String fullInput = event.getFullCommandName().substring(event.getName().length()).trim();
        String[] parts = fullInput.split(" ");

        Command command = baseCommand;
        for (String part : parts) {

            List<Command> subcommands = command.getArguments().stream()
                    .filter(argument -> argument.argument() instanceof SubCommandArgument)
                    .flatMap(argument -> ((SubCommandArgument) argument.argument()).getSubcommands().stream())
                    .toList();

            Command subcommand = subcommands.stream()
                    .filter(sc -> sc.getName().equals(part) || sc.getAliases().contains(part))
                    .findAny()
                    .orElse(null);

            if (subcommand == null) {
                break;
            }

            command = subcommand;

        }

        return command;

    }

    private boolean permissionCheck(@NotNull Command command, @NotNull SlashCommandInteractionEvent event) {

        Permission permission = command.getPermission();
        if (event.isFromGuild() && permission != null) {

            Member member = event.getMember();

            assert member != null;

            boolean hasPermission = permissionManager.hasPermission(member,  permission);
            if (!hasPermission) {
                messages.reply(event.getInteraction(),"sbds.no-permission", event.getUser())
                        .withPlaceholders("{PERMISSION}", permission)
                        .queue();
                return false;
            }

        }

        return true;

    }


    public static class RegisteredSlashCommand extends AbstractCommandManager.RegisteredCommand<IRegisteredSlashCommand, ISlashCommandManager> implements IRegisteredSlashCommand {

        public RegisteredSlashCommand(
                @NotNull ISlashCommandManager manager,
                @NotNull Command command
        ) {
            super(manager, command);
        }

    }

}
