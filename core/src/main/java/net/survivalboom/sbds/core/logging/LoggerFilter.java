package net.survivalboom.sbds.core.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.registration.InternalRegistrationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoggerFilter extends Manager {

    private static final Logger log = LoggerFactory.getLogger(LoggerFilter.class);

    private final SBDS sbds;

    private final InternalRegistrationManager<RegisteredLoggerFilter> registry;

    private Level minimalLoggingLevel = Level.INFO;


    public LoggerFilter(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.registry = new InternalRegistrationManager<>(this, null, sbds.getRegistrationRegistry());
    }


    @Override
    protected void init0() {
        registry.init();
        reload();
        LoggerLayout.INSTANCE.setLoggerFilter(this);
    }

    @Override
    protected void shutdown0() {
        LoggerLayout.INSTANCE.setLoggerFilter(null);
        registry.shutdown();
    }

    public boolean process(@NotNull ILoggingEvent event) {

        boolean result = false;
        for (var reg : registry.getRegisteredObjects()) {

            var function = reg.function;

            boolean r;
            try {
                r = function.apply(event);
            }

            catch (Throwable t) {
                log.error("Logger filter `{}` throw an exception!", reg.registration.key(), t);
                continue;
            }

            if (!result) {
                result = r;
            }

        }

        return result;

    }

    //
    // LOGGING MINIMAL LEVEL
    //

    public @NotNull org.slf4j.event.Level getMinimalLevel() {
        return minimalLoggingLevel;
    }

    public void setMinimalLoggingLevel(@NotNull Level level) {

        Objects.requireNonNull(level, "level == null");
        checkValid();

        this.minimalLoggingLevel = level;

    }

    //
    // FILTERING RULES
    //

    public @NotNull RegisteredLoggerFilter registerFilteringRule0(@Nullable IModule module, @NotNull String name, @NotNull Function<ILoggingEvent, Boolean> function) {

        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(function, "function == null");
        checkValid();

        RegisteredLoggerFilter registeredLoggerFilter = new RegisteredLoggerFilter(this, function);
        registeredLoggerFilter.registration = registry.register0(module, name, registeredLoggerFilter);

        return registeredLoggerFilter;

    }

    public boolean unregisterFilterRule(@NotNull RegisteredLoggerFilter reg) {
        return registry.unregister(reg) != null;
    }

    public @NotNull List<RegisteredLoggerFilter> getRules() {
        return registry.getRegisteredObjects();
    }

    //
    // CONFIG
    //

    public void reload() {

        var unregList = registry.getRegistrations().stream()
                .filter(reg -> reg.key().prefix().equals("sbds"))
                .toList();

        unregList.forEach(registry::unregister);
        minimalLoggingLevel = Level.INFO;

        ConfigurationNode section = sbds.getConfiguration().node("logging");
        if (section.virtual()) {
            return;
        }

        ConfigurationNode levelNode = section.node("global-level");
        if (!levelNode.virtual()) {

            try {
                minimalLoggingLevel = levelNode.get(Level.class);
            } catch (SerializationException e) {
                log.error("Invalid minimal logging level `{}`. Valid options are: {}", levelNode.getString(), String.join(", ", Arrays.stream(Level.values()).map(Enum::toString).toList()));
            }

        }

        ConfigurationNode rulesSection = section.node("rules");
        if (!rulesSection.virtual()) {

            var result = loadFromSection0(null, rulesSection);

            for (var entry : result.failed.entrySet()) {
                log.error("Failed to load logger rule `{}`.", entry.getKey(), entry.getValue());
            }

        }

    }

    public @NotNull MassLoadResult loadFromSection0(@Nullable IModule module, @NotNull ConfigurationNode section) {

        MassLoadResult result = new MassLoadResult(new ArrayList<>(), new HashMap<>());

        for (ConfigurationNode rule : section.childrenMap().values()) {

            try {

                boolean levelRule = rule.hasChild("level");
                Function<ILoggingEvent, Boolean> function;

                String patternRaw = rule.node("pattern").getString();
                if (patternRaw == null) {
                    throw new IllegalArgumentException("Pattern of logging rule is not defined");
                }

                Pattern pattern = Pattern.compile(patternRaw);

                if (levelRule) {

                    ConfigurationNode levelNode = rule.node("level");

                    Level level;
                    try {
                        level = levelNode.get(Level.class);
                    }

                    catch (SerializationException e) {
                        throw new IllegalArgumentException("Invalid filter level `" + rule.getString() + "`");
                    }

                    function = createLoggerLevelRule(pattern, level);

                }

                else {
                    function = createLoggerIgnoreRule(pattern);
                }

                RegisteredLoggerFilter registeredLoggerFilter = registerFilteringRule0(module, String.valueOf(rule.key()), function);
                result.loaded.add(registeredLoggerFilter);

            }

            catch (Exception e) {
                result.failed.put(String.valueOf(rule.key()), e);
            }

        }

        return result;

    }

    public record MassLoadResult(@NotNull List<RegisteredLoggerFilter> loaded, @NotNull Map<String, Exception> failed) {}

    //
    // RULES
    //

    public @NotNull Function<ILoggingEvent, Boolean> createLoggerIgnoreRule(
            @NotNull Pattern pattern
    ) {
        return event -> {
            Matcher matcher = pattern.matcher(event.getLoggerName());
            return matcher.find();
        };
    }

    public @NotNull Function<ILoggingEvent, Boolean> createLoggerLevelRule(
            @NotNull Pattern pattern,
            @Nullable Level level
    ) {
        return event -> {

            Matcher matcher = pattern.matcher(event.getLoggerName());
            if (!matcher.find()) {
                return false;
            }

            ch.qos.logback.classic.Level lvl = event.getLevel();
            return level != null && !lvl.isGreaterOrEqual(ch.qos.logback.classic.Level.convertAnSLF4JLevel(level));

        };
    }


    public static class RegisteredLoggerFilter {

        private final LoggerFilter manager;

        private Registration<RegisteredLoggerFilter> registration;

        private final Function<ILoggingEvent, Boolean> function;


        public RegisteredLoggerFilter(
                @NotNull LoggerFilter manager,
                @NotNull Function<ILoggingEvent, Boolean> function
        ) {
            this.manager = manager;
            this.function = function;
        }


        public @NotNull Function<ILoggingEvent, Boolean> getFunction() {
            return function;
        }

        public @NotNull Registration<RegisteredLoggerFilter> getRegistration() {
            return registration;
        }

        public @NotNull LoggerFilter getManager() {
            return manager;
        }

    }

}
