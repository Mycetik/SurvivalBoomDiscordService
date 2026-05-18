package net.survivalboom.sbds.core;

import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.core.libraries.DynamicClassLoader;
import net.survivalboom.sbds.core.libraries.simple.SimpleLibrariesDownloader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Main {

    private static boolean started = false;

    public static void main(String[] args) throws Throwable {

        if (started) {
            throw new RuntimeException("Ну ты долбоеб, да? Я склоняюсь к мысли что да");
        }

        started = true;

        System.out.printf("Starting SBDS v%s ...\n", BuildConstants.VERSION);

        File thisFile = CommonUtils.getJarFile(Main.class);
        File workingDir = thisFile.getParentFile();

        DynamicClassLoader classLoader = new DynamicClassLoader("SBDS", Main.class.getClassLoader());
        classLoader.addParentDelegateClassRule("IgnoreInitClasses", name -> name.equals("net.survivalboom.sbds.core.Main") ||name.contains("DynamicClassLoader") || name.startsWith("net.survivalboom.sbds.core.libraries.simple"));
        classLoader.addSource(thisFile);

        Thread.currentThread().setContextClassLoader(classLoader);

        var lib = checkInitialLibraries(workingDir, classLoader);
        launch(workingDir, lib, classLoader);

        System.exit(0);

    }

    private static @NotNull SimpleLibrariesDownloader checkInitialLibraries(@NotNull File workingDir, @NotNull DynamicClassLoader classLoader) {

        System.out.println("Loading initial libraries...");

        SimpleLibrariesDownloader librariesDownloader = new SimpleLibrariesDownloader(new File(workingDir, "libraries"), classLoader);

        // Logging //
        var slf4j = librariesDownloader.download("https://repo1.maven.org/maven2/", "org.slf4j", "slf4j-api", "2.0.16");
        var logbackCore = librariesDownloader.download("https://repo1.maven.org/maven2/", "ch.qos.logback", "logback-core", "1.5.16");
        var logbackClassic = librariesDownloader.download("https://repo1.maven.org/maven2/", "ch.qos.logback", "logback-classic", "1.5.16");

        logbackClassic.dependencies().add(logbackCore);
        logbackClassic.dependencies().add(slf4j);

        // configuration

        var geantyref = librariesDownloader.download("https://repo1.maven.org/maven2/", "io.leangen.geantyref", "geantyref", "2.0.1");
        var option = librariesDownloader.download("https://repo1.maven.org/maven2/", "net.kyori", "option", "1.1.0");

        var configurateCore = librariesDownloader.download("https://repo1.maven.org/maven2/", "org.spongepowered", "configurate-core", "4.2.0");

        configurateCore.dependencies().add(geantyref);
        configurateCore.dependencies().add(option);

        var configurateYaml = librariesDownloader.download("https://repo1.maven.org/maven2/", "org.spongepowered", "configurate-yaml", "4.2.0");
        var configurateXml = librariesDownloader.download("https://repo1.maven.org/maven2/", "org.spongepowered", "configurate-xml", "4.2.0");

        configurateYaml.dependencies().add(configurateCore);
        configurateXml.dependencies().add(configurateCore);

        return librariesDownloader;

    }

    private static void launch(@NotNull File workingDir, @NotNull SimpleLibrariesDownloader downloader, @NotNull DynamicClassLoader loader) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Class<?> bootstrapClass = loader.getClass("net.survivalboom.sbds.core.SbdsBootstrap", false, false);
        if (bootstrapClass == null) {
            throw new ClassNotFoundException("net.survivalboom.sbds.core.SbdsBootstrap");
        }

        Constructor<?> constructor = bootstrapClass.getDeclaredConstructors()[0];
        Object bootstrapInstance = constructor.newInstance(workingDir, downloader, loader);

        bootstrapClass.getMethod("launch").invoke(bootstrapInstance);

    }

    public static void exit() {
        CommonUtils.sleep(10000);
        System.exit(1);
    }

}
