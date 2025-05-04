package net.survivalboom.sbds.api.commands.base;

import net.survivalboom.sbds.api.commands.CommandExecutor;
import net.survivalboom.sbds.api.commands.ExecutionInfo;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.modules.IModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class CommandBase implements CommandExecutor {

    private final String name;


    private final String description;

    private final String usage;


    private final String permission;

    private final boolean defaultPermission;


    private final List<String> aliases = new ArrayList<>();

    private final List<net.survivalboom.sbds.api.commands.CommandArgument> arguments = new ArrayList<>();

    private final Set<CommandBase> subcommands = new HashSet<>();


    public CommandBase() {

        Command info = getInfoAnnotation();

        this.name = info.name();

        this.description = info.description().isEmpty() ? null : info.description();
        this.usage = info.usage().isEmpty() ? null : info.usage();

        this.permission = info.permission().isEmpty() ? null : info.permission();
        this.defaultPermission = info.defaultPermission();

                Objects.requireNonNull(info.aliases(), "aliases == null");
        Objects.requireNonNull(name, "name == null");

        aliases.addAll(List.of(info.aliases()));
        arguments.addAll(scanForArguments());

    }

    private @NotNull Command getInfoAnnotation() {
        Command annotation = this.getClass().getAnnotation(Command.class);
        if (annotation == null) throw new IllegalStateException("Annotation @Command is not present!");
        return annotation;
    }

    private @NotNull List<net.survivalboom.sbds.api.commands.CommandArgument> scanForArguments() {


        List<net.survivalboom.sbds.api.commands.CommandArgument> out = new ArrayList<>();

        for (Method method : getClass().getDeclaredMethods()) {

            if (!method.isAnnotationPresent(CommandArgument.class)) continue;

            CommandArgument argumentInfo = method.getAnnotation(CommandArgument.class);

            String name = argumentInfo.name();

            //noinspection ConstantValue
            if (name == null) throw InvalidCommandException.createInvalidArgumentException(method, "Argument name is null", null);


            Argument<?> argument;
            try {
                argument = (Argument<?>) method.invoke(this);
            }

            catch (IllegalAccessException e) {
                throw InvalidCommandException.createInvalidArgumentException(method, "Method must be public", null);
            }

            catch (InvocationTargetException e) {
                throw InvalidCommandException.createInvalidArgumentException(method, "Error occurred", e);
            }

            catch (IllegalArgumentException e) {
                throw InvalidCommandException.createInvalidArgumentException(method, "Method must not have any parameters", null);
            }

            catch (ClassCastException e) {
                throw InvalidCommandException.createInvalidArgumentException(method, "Method return type must be 'Argument<T>'.", null);
            }

            if (argument == null) throw InvalidCommandException.createInvalidArgumentException(method, "Method returned null", null);

            out.add(new net.survivalboom.sbds.api.commands.CommandArgument(name, argumentInfo.description(), argument, argumentInfo.index(), argumentInfo.required()));

        }

        out.sort(Comparator.comparing(net.survivalboom.sbds.api.commands.CommandArgument::index));

        return out;

    }


    public void addSubCommand(@NotNull CommandBase commandBase) {
        subcommands.add(commandBase);
    }


    public @NotNull net.survivalboom.sbds.api.commands.Command build(@Nullable IModule module) {

        net.survivalboom.sbds.api.commands.Command command = new net.survivalboom.sbds.api.commands.Command(name, module);

        command.withDescription(description);
        command.withUsage(usage);
        command.withAliases(aliases);
        command.withPermission(permission, defaultPermission);

        if (!subcommands.isEmpty()) {
            subcommands.forEach(c -> command.withSubcommand(c, module));
            command.executes(this::subcommandProxy);
            return command;
        }

        command.withArguments(arguments);

        command.executes(this);

        return command;

    }


    private void subcommandProxy(@NotNull ExecutionInfo info) {

        net.survivalboom.sbds.api.commands.Command command = Objects.requireNonNull(info.arguments().get("subcommand", net.survivalboom.sbds.api.commands.Command.class));


    }



    @Override
    public void execute(@NotNull ExecutionInfo info) throws Throwable {

        try {

            Method method = this.getClass().getDeclaredMethod("executes", info.getClass());

            method.invoke(this, info);

        }

        catch (NoSuchMethodException | IllegalAccessException e) {
            throw new InvalidCommandException("Executor for " + info.getClass().getName() + " not found");
        }

        catch (InvocationTargetException e) {
            throw e.getCause();
        }

    }



    public final @NotNull String getName() {
        return name;
    }

    public final @Nullable String getDescription() {
        return description;
    }

    public final @Nullable String getPermission() {
        return permission;
    }

    public final @Nullable String getUsage() {
        return usage;
    }

    public final @NotNull List<String> getAliases() {
        return new ArrayList<>(aliases);
    }

    public final @NotNull List<net.survivalboom.sbds.api.commands.CommandArgument> getArguments() {
        return arguments;
    }

}
