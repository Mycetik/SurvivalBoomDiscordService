package net.survivalboom.sbds.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.LayoutBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Я їбав розбиратись із XML. Який дебіл взагалі придумав конфігурувати логер через xml?
// Краще самому написати, тоді я точно буду знати що усе працює як я хочу.
public class LoggerLayout extends LayoutBase<ILoggingEvent> {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss");

    public static final String RESET = "\u001B[0m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String RED = "\u001B[31m";
    public static final String YELLOW = "\u001B[33m";
    public static final String GREEN = "\u001B[32m";
    public static final String CYAN = "\u001B[36m";
    public static final String BLUE = "\u001B[34m";

    public static Map<String, String> COLOR_MAP = Map.of(
            "&4", RED,
            "&c", BRIGHT_RED,
            "&r", RESET,
            "&e", YELLOW,
            "&a", GREEN,
            "&3", CYAN,
            "&9", BLUE
    );

    public static boolean colors = true;


    @Override
    public String doLayout(ILoggingEvent event) {

        try {

            return doLayout0(event);

        }

        catch (Throwable t) {
            System.err.println("[LoggerLayout] An exception was thrown while attempting to parse ` " + event.getMessage() + "`");
            t.printStackTrace();
            throw t;
        }

    }


    private String doLayout0(ILoggingEvent event) {

        String loggerName = event.getLoggerName();
        boolean isRoot = loggerName.equals(Logger.ROOT_LOGGER_NAME);
        String timeFormatted = dtf.format(LocalDateTime.now());

        String loggerNamePart = !isRoot ? "&r/&3" + loggerName  : "";
        String levelColored = colorLevel(event.getLevel().levelStr);

        String messageFormatted = parsePlaceholders(event);

        String str = String.format("[%s %s%s&r]: %s&r\n", timeFormatted, levelColored, loggerNamePart, messageFormatted);


        Throwable throwable = getThrowable(event);

        if (throwable != null) {

            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            throwable.printStackTrace(printWriter);

            str = str + stringWriter;

        }

        return color(str);

    }

    private String parsePlaceholders(ILoggingEvent event) {

        String msg = event.getMessage();

        Object[] array = event.getArgumentArray();
        if (array == null) return msg;

        for (Object o : array) {
            msg = msg.replaceFirst(Pattern.quote("{}"), Matcher.quoteReplacement(String.valueOf(Objects.requireNonNullElse(o, "null"))));
        }

        return msg;

    }

    private @Nullable Throwable getThrowable(ILoggingEvent event) {

        ThrowableProxy throwableProxy = (ThrowableProxy) event.getThrowableProxy();
        if (throwableProxy == null) {

            Object[] array = event.getArgumentArray();
            if (array == null) return null;

            List<Object> args = List.of(array);
            if (args.isEmpty()) return null;

            if (args.getLast() instanceof Throwable t) {
                return t;
            }

            return null;

        }

        return throwableProxy.getThrowable();

    }

    private String colorLevel(@NotNull String levelStr) {

        return switch (levelStr) {
            case "ERROR" -> "&cERROR";
            case "WARN" -> "&eWARN";
            case "INFO" -> "&aINFO";
            case "DEBUG" -> "&9DEBUG";
            case "TRACE" -> "&9TRACE";
            default -> levelStr;
        };

    }

    private String color(String string) {

        for (Map.Entry<String, String> entry : COLOR_MAP.entrySet()) {

            String code = entry.getKey();
            String color = colors ? entry.getValue() : "";

            string = string.replace(code, color);

        }

        return string;

    }


    public static @NotNull LoggerLayout setup() {

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        LoggerLayout loggerLayout = new LoggerLayout();

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setLayout(loggerLayout);
        consoleAppender.start();

        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAndStopAllAppenders();
        rootLogger.addAppender(consoleAppender);
        rootLogger.setLevel(Level.INFO);
        rootLogger.setAdditive(true);

        return loggerLayout;

    }

}
