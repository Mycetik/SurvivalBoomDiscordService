package net.survivalboom.sbds.core.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ch.qos.logback.classic.Level;

import java.util.regex.Pattern;

public record LoggerRule(@NotNull String name, @NotNull Pattern pattern, boolean ignore, @Nullable Level level) {

    public boolean ignore(@NotNull ILoggingEvent event) {

        if (ignore) return true;

        Level lvl = event.getLevel();
        return level != null && !lvl.isGreaterOrEqual(level);

    }

}
