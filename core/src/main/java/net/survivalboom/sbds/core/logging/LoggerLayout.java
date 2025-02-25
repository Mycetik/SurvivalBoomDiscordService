package net.survivalboom.sbds.core.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.LayoutBase;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

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


    private static LoggerLayout current = null;


    private boolean colorSupport = true;

    private String stackTraceFormat = "    at {CLASS}.{METHOD}({FILE}:{LINE}) ~[{CLASSLOADER}:{MODULE}]";


    public void colorSupport(boolean v) {
        this.colorSupport = v;
    }

    public boolean colorSupport() {
        return colorSupport;
    }


    public void stackTraceFormat(@NotNull String format) {
        Objects.requireNonNull(format, "format is null");
        this.stackTraceFormat = format;
    }



    @Override
    public String doLayout(ILoggingEvent event) {

        String loggerName = event.getLoggerName();
        boolean isRoot = loggerName.equals(Logger.ROOT_LOGGER_NAME);

        String timeFormatted = dtf.format(LocalDateTime.now());

        StringBuilder builder = new StringBuilder();

        addColor(RESET, builder);

        builder.append("[");
        builder.append(timeFormatted);
        builder.append(" ");

        addColor(getColorForLevel(event.getLevel()), builder);
        builder.append(event.getLevel().toString());
        addColor(RESET, builder);

        if (!isRoot) {
            builder.append("/");
            addColor(CYAN, builder);
            builder.append(loggerName);
            addColor(RESET, builder);
        }

        builder.append("]: ");

        String msgWithReplacements = event.getMessage();
        if (event.getArgumentArray() != null) {
            for (Object o : event.getArgumentArray()) {
                msgWithReplacements = msgWithReplacements.replaceFirst("\\{}", String.valueOf(Objects.requireNonNullElse(o, "null")));
            }
        }

        builder.append(msgWithReplacements);

        builder.append("\n");

        addStackTrace(event, builder);

        return builder.toString();

    }

    private void addColor(@NotNull String clr, @NotNull StringBuilder builder) {
        if (!colorSupport) return;
        builder.append(clr);
    }

    private @NotNull String getColorForLevel(@NotNull Level level) {

        String levelStr = level.toString();

        return switch (levelStr) {
            case "ERROR" -> BRIGHT_RED;
            case "WARN" -> YELLOW;
            case "INFO" -> GREEN;
            case "DEBUG" -> CYAN;
            case "TRACE" -> BLUE;
            default -> RESET;
        };

    }

    private static void addStackTrace(@NotNull ILoggingEvent event, @NotNull StringBuilder builder) {

        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy == null) {

            Object[] argumentArray = event.getArgumentArray();
            if (argumentArray == null || argumentArray.length == 0) return;

            Object object = argumentArray[0];
            if (!(object instanceof Throwable t)) return;

            throwableProxy = new ThrowableProxy(t);

        }

        throwableProxy = getEdgeCause(throwableProxy);

        builder.append(throwableProxy.getClassName()).append(": ").append(throwableProxy.getMessage()).append("\n");

        for (StackTraceElementProxy element : throwableProxy.getStackTraceElementProxyArray()) {
            builder.append(stackTraceElementString(element.getStackTraceElement()));
            builder.append("\n");
        }

    }

    public static String stackTraceElementString(@NotNull StackTraceElement element) {

        String module = element.getModuleVersion();
        String classLoader = element.getClassLoaderName();

        Placeholders placeholders = new Placeholders();
        placeholders.add("{CLASS}", element.getClassName());
        placeholders.add("{METHOD}", element.getMethodName());
        placeholders.add("{FILE}", Objects.requireNonNullElse(element.getFileName(), "?"));
        placeholders.add("{LINE}", element.getLineNumber());
        placeholders.add("{CLASSLOADER}", classLoader == null ? "?" : classLoader);
        placeholders.add("{MODULE}", module == null ? "?" : module);

        return placeholders.parse(current().stackTraceFormat);

    }

    private static @NotNull IThrowableProxy getEdgeCause(@NotNull IThrowableProxy origin) {

        while (origin.getCause() != null) {
            origin = origin.getCause();
        }

        return origin;

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

        current = loggerLayout;

        return loggerLayout;

    }

    public static @NotNull LoggerLayout current() {
        return current;
    }

}
