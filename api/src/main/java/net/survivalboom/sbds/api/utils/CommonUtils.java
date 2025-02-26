package net.survivalboom.sbds.api.utils;

import org.bspfsystems.yamlconfiguration.configuration.Configuration;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CommonUtils {


    public static String STACK_TRACE_FORMAT = "    at {CLASS}.{METHOD}({FILE}:{LINE}) ~[{CLASSLOADER}:{MODULE}]";


    //
    // FILES
    //

    public static void checkFiles(@NotNull Class<?> origin, @NotNull File workingDir, @NotNull Map<String, String> files, @Nullable Logger logger) {

        if (workingDir.isFile()) throw new IllegalArgumentException(String.format("File at %s is a file!", workingDir.getPath()));

        try {

            if (!workingDir.exists()) workingDir.mkdirs();

            // Получаем путь к JAR файлу
            File jarFile = new File(origin.getProtectionDomain().getCodeSource().getLocation().toURI());

            // Открываем JAR как ZipFile
            try (ZipFile zipFile = new ZipFile(jarFile)) {

                for (Map.Entry<String, String> entry : files.entrySet()) {

                    String jarPath = entry.getKey();
                    String destinationPath = entry.getValue();
                    File destFile = new File(workingDir, destinationPath);

                    if (jarPath.endsWith("/")) {
                        // Если ключ заканчивается на "/", это папка — копируем её содержимое рекурсивно
                        copyDirectoryFromJar(zipFile, jarPath, destFile, logger);
                    }

                    else {

                        // Если это отдельный файл, копируем его напрямую
                        if (!destFile.exists()) {
                            copyFileFromJar(zipFile, jarPath, destFile, logger);
                        }

                    }

                }

            }

        }

        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void copyFileFromJar(ZipFile zipFile, String jarPath, File destFile, @Nullable Logger logger) throws IOException {

        ZipEntry entry = zipFile.getEntry(jarPath);
        if (entry == null) throw new IOException("File " + jarPath + " not found in JAR.");

        destFile.getParentFile().mkdirs();

        try (InputStream stream = zipFile.getInputStream(entry)) {
            Files.copy(stream, destFile.toPath());
            if (logger != null) logger.info("Created {}...", destFile.getName());
        }

    }

    private static void copyDirectoryFromJar(ZipFile zipFile, String jarDirPath, File destDir, @Nullable Logger logger) throws IOException {

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {

            ZipEntry entry = entries.nextElement();
            String entryName = entry.getName();

            if (!entryName.startsWith(jarDirPath)) continue;

            String relativePath = entryName.substring(jarDirPath.length());
            File destFile = new File(destDir, relativePath);

            if (destFile.exists()) continue;

            if (entry.isDirectory()) destFile.mkdirs();
            else copyFileFromJar(zipFile, entryName, destFile, logger);

        }
    }

    public static @NotNull File getJarFile(@NotNull Class<?> clazz) {

        String jarPath;

        try {
            jarPath = clazz.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        }

        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String decodedPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);

        return new File(decodedPath);

    }

    //
    // LOGGING
    //

    public static void logThreadStackTrace(@NotNull Logger logger, @NotNull Level level, @NotNull Thread thread) {

        String msg = "Thread dump of: " + thread.getName() + "\n";

        msg += stackTraceToString(thread.getStackTrace());

        logger.atLevel(level).log(msg);

    }

    public static @NotNull String stackTraceToString(@NotNull StackTraceElement[] stackTraceElements) {

        StringBuilder builder = new StringBuilder();

        for (StackTraceElement element : stackTraceElements) {
            builder.append(stackTraceElementToString(element));
            builder.append("\n");
        }

        return builder.toString();

    }

    public static String stackTraceElementToString(@NotNull StackTraceElement element) {

        String module = element.getModuleVersion();
        String classLoader = element.getClassLoaderName();

        Placeholders placeholders = new Placeholders();
        placeholders.add("{CLASS}", element.getClassName());
        placeholders.add("{METHOD}", element.getMethodName());
        placeholders.add("{FILE}", Objects.requireNonNullElse(element.getFileName(), "?"));
        placeholders.add("{LINE}", element.getLineNumber());
        placeholders.add("{CLASSLOADER}", classLoader == null ? "?" : classLoader);
        placeholders.add("{MODULE}", module == null ? "?" : module);

        return placeholders.parse(STACK_TRACE_FORMAT);

    }

    //
    // WAIT
    //

    public static void sleep(int millis) {
        if (millis <= 0) return;
//        if (Bukkit.isPrimaryThread()) throw new IllegalStateException("Do not attempt to freeze main thread! Fuck yourself!");
        try { Thread.sleep(millis); }
        catch (InterruptedException ignored) {}
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public static void waitUntil(@NotNull Supplier<Boolean> supplier, long timeOutInMillis, @Nullable Runnable onCheck) {

        long startTime = System.currentTimeMillis();

        while (!supplier.get()) {

            if (onCheck != null) onCheck.run();

            long currentTime = System.currentTimeMillis();
            if (timeOutInMillis > 0 && (currentTime - startTime) > timeOutInMillis) {
                throw new RuntimeException("waitUntil method time out");
            }

            sleep(10);

        }

    }

    public static void waitUntil(@NotNull Supplier<Boolean> supplier, long timeOutMillis) {
        waitUntil(supplier, timeOutMillis, null);
    }

    public static void waitUntil(@NotNull Supplier<Boolean> supplier) {
        waitUntil(supplier, 0, null);
    }


    //
    // ENUM
    //

    public static @Nullable <E extends Enum<E>> E getEnumValue(@NotNull Class<E> clazz, @Nullable String value) {

        if (value == null) return null;


        try {
            return Enum.valueOf(clazz, value);
        }

        catch (IllegalArgumentException e) {
            return null;
        }

    }


    //
    // REFLECTION
    //

    public static @NotNull StackTraceElement getMethodCaller(int depth) {

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();

        depth += 2;

        return elements[depth];

    }

    public static @Nullable Object invokeMethod(@NotNull Object origin, @NotNull Method method, Object... resources) {

        // Создаем карту доступных ресурсов с ключами по их классам
        Map<Class<?>, Object> resourceMap = new HashMap<>();
        for (Object resource : resources) {
            if (resource == null) continue;
            resourceMap.put(resource.getClass(), resource);
        }

        // Получаем параметры метода
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];

        // Подбираем ресурсы для каждого параметра метода
        for (int i = 0; i < parameterTypes.length; i++) {

            Class<?> paramType = parameterTypes[i];

            if (resourceMap.containsKey(paramType)) arguments[i] = resourceMap.get(paramType);

            else {

                String errorMsg = String.format("Failed to invoke %s.%s. No resource found for parameter type: %s", method.getDeclaringClass().getSimpleName(), method.getName(), paramType.getName());

                throw new IllegalArgumentException(errorMsg);

            }

        }

        // Делаем метод доступным, если он private
        if (!method.canAccess(origin)) {
            method.setAccessible(true);
        }

        // Вызываем метод
        try {
            return method.invoke(origin, arguments);
        }

        catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }


    //
    // YAML
    //

    public static @NotNull ConfigurationSection getOrCreateSection(@NotNull ConfigurationSection configuration, @NotNull String path) {

        Objects.requireNonNull(configuration, "configuration == null");
        Objects.requireNonNull(path, "path == null");

        ConfigurationSection out = configuration.getConfigurationSection(path);
        if (out == null) out = configuration.createSection(path);

        return out;

    }

    public static @NotNull Properties getPropertiesFromYaml(@NotNull ConfigurationSection section) {

        Properties properties = new Properties();
        properties.putAll(getStringMapFromYaml(section));

        return properties;

    }

    public static @NotNull Map<String, String> getStringMapFromYaml(@NotNull ConfigurationSection section) {

        Map<String, String> map = new HashMap<>();
        section.getKeys(false).forEach(k -> loadPropertiesMap(section, map, k));

        return map;

    }

    private static void loadPropertiesMap(@NotNull ConfigurationSection configuration, @NotNull Map<String, String> map, @NotNull String path) {

        ConfigurationSection section = configuration.getConfigurationSection(path);
        if (section != null) {
            section.getKeys(false).forEach(k -> loadPropertiesMap(configuration, map, path + "." + k));
            return;
        }

        map.put(path, configuration.getString(path));

    }


}
