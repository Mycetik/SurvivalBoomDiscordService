package net.survivalboom.sbds.modules.music.utils;

import dev.arbjerg.lavalink.client.NodeOptions;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.valid.Manager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

public class IntegratedLavalinkManager extends Manager {

    public static final URI DOWNLOAD_LINK = URI.create("https://github.com/lavalink-devs/Lavalink/releases/download/4.2.2/Lavalink.jar");

    private final ModuleMain module;

    private final Logger logger;


    private final File lavalinkFolder;

    private final File lavalinkFile;

    private final File lavalinkConfig;


    private boolean enabled;

    private int port = 0;

    private Process lavalinkProcess;


    public IntegratedLavalinkManager(@NotNull ModuleMain main) {

        this.module = main;
        this.logger = main.getLogger();

        this.lavalinkFolder = new File(main.getDataFolder(), "lavalink");
        this.lavalinkFile = new File(lavalinkFolder, "Lavalink.jar");
        this.lavalinkConfig = new File(lavalinkFolder, "application.yml");

    }


    @Override
    protected void init0() {

        enabled = !module.getConfig().node("manual-setup").getBoolean();
        if (!enabled) {
            logger.info("Manual lavalink setup! You are on your own!");
            return;
        }

        if (!lavalinkFile.exists()) {

            logger.info("Downloading lavalink from `{}`...", DOWNLOAD_LINK);
            module.checkFiles("application.yml", "lavalink/application.yml");

            try (HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()) {

                HttpRequest request = HttpRequest.newBuilder().uri(DOWNLOAD_LINK).header("User-Agent", "Mozilla/5.0").GET().build();
                HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

                try (InputStream stream = response.body()) {

                    int statusCode = response.statusCode();
                    if (statusCode != 200) {
                        logger.error("Failed to download lavalink! Status code: {}; {}", statusCode, new String(stream.readAllBytes()));
                        throw new RuntimeException("Automatic lavalink setup failed");
                    }

                    try (FileOutputStream out = new FileOutputStream(lavalinkFile)) {
                        stream.transferTo(out);
                    }

                }


            }

            catch (Throwable t) {
                logger.error("Failed to download lavalink!", t);
                throw new RuntimeException("Automatic lavalink setup failed", t);
            }

        }

        port = findFreePort(20);
        if (port == 1) {
            logger.info("Could not found a port for lavalink in 20 attempts!");
            throw new RuntimeException("Automatic lavalink setup failed");
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .nodeStyle(NodeStyle.BLOCK)
                .path(lavalinkConfig.toPath())
                .build();

        try {

            ConfigurationNode node = loader.load();

            node.node("server", "port").set(port);
            node.node("lavalink", "server", "password").set("survivalboom");

            loader.save(node);

        }

        catch (Exception e) {
            throw new RuntimeException(e);
        }


        if (!startLavalink()) {
            throw new RuntimeException("Automatic lavalink setup failed");
        }

    }

    @Override
    protected void shutdown0() {

        if (!enabled) return;
        stopLavalink();

    }


    private boolean startLavalink() {

        logger.info("Starting lavalink on [127.0.0.1:{}]...", port);

        ProcessBuilder builder = new ProcessBuilder("java", "-jar", lavalinkFile.getAbsolutePath());
        builder.environment().clear();
        builder.directory(lavalinkFolder);

//        builder.inheritIO();

        try {
             lavalinkProcess = builder.start();
        }

        catch (IOException e) {
            logger.error("Failed to start lavalink server!", e);
            return false;
        }

        try {
            CommonUtils.waitUntil(this::checkLavalink, 30000);
        }

        catch (RuntimeException e) {
            logger.error("Could not connect to lavalink. Looks like it's dead.");
            return false;
        }

        return true;

    }

    private void stopLavalink() {

        logger.info("Stopping lavalink...");

        lavalinkProcess.destroy();

        try {
            CommonUtils.waitUntil(lavalinkProcess::isAlive, 30000);
        }

        catch (RuntimeException e) {
            logger.warn("Lavalink process didn't die in 10 seconds, killing process...");
            lavalinkProcess.destroyForcibly();
        }

    }

    private boolean checkLavalink() {

        try {

            HttpURLConnection connection = (HttpURLConnection) URI.create("http://127.0.0.1:" + port + "/version").toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            return connection.getResponseCode() == 401;

        }

        catch (IOException e) {
            return false;
        }

    }


    private int findFreePort(int attempts) {

        for (int i = 0; i < attempts; i++) {

            int port = new Random().nextInt(1024, 65535);

            try (ServerSocket socket = new ServerSocket(port)) {
                socket.setReuseAddress(true);
                return port;
            }

            catch (IOException ignored) {}

        }

        return -1;

    }

    public @NotNull NodeOptions createNodes() {

        NodeOptions.Builder builder = new NodeOptions.Builder();
        builder.setName("manual-setup");
        builder.setServerUri("http://127.0.0.1:" + port);
        builder.setPassword("survivalboom");

        return builder.build();

    }

    public boolean isEnabled() {
        return enabled;
    }

}
