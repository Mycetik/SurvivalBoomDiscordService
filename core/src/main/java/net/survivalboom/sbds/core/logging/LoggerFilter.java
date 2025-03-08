package net.survivalboom.sbds.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.core.SBDS;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoggerFilter extends Manager {

    private final LoggerLayout layout = LoggerLayout.layout;

    private final Set<LoggerRule> rules = new HashSet<>();

    private final YamlConfiguration configuration;


    public LoggerFilter(@NotNull YamlConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void init0() {

        ConfigurationSection loggingSection = configuration.getConfigurationSection("logging");
        if (loggingSection == null) return;

        String levelRaw = loggingSection.getString("global-level");
        layout.rootLogger.setLevel(Level.toLevel(levelRaw));

        ConfigurationSection rulesSection = loggingSection.getConfigurationSection("rules");
        if (rulesSection == null) return;

        for (String s : rulesSection.getKeys(false)) {

            ConfigurationSection section = rulesSection.getConfigurationSection(s);
            if (section == null) continue;

            String target = section.getString("target");
            if (target == null) continue;

            String levelRaw2 = section.getString("level");
            Level level = Level.valueOf(levelRaw2);

            boolean ignore = section.getBoolean("ignore");

            LoggerRule rule = new LoggerRule(s, Pattern.compile(target), ignore, level);
            rules.add(rule);

        }

    }

    @Override
    protected void shutdown0() {
        rules.clear();
    }

    public boolean process(@NotNull ILoggingEvent event) {

        for (LoggerRule rule : rules) {

            Matcher matcher = rule.pattern().matcher(event.getLoggerName());
            if (!matcher.find()) {
                continue;
            }

            return rule.ignore(event);

        }

        return false;

    }

}
