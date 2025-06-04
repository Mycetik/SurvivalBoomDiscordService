package net.survivalboom.sbds.modules.github;


import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.github.commands.GithubCommand;
import net.survivalboom.sbds.modules.github.storage.WebhookRepositoryHandler;
import net.survivalboom.sbds.modules.github.webhook.WebHookListener;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

public class GithubModule extends ModuleMain {

    private WebHookListener webHookListener;

    @Override
    public void onEnable() throws Throwable {

        checkFiles(Map.of(
                "config.yml", "config.yml",
                "translations/translation_uk.yml", "translations/translation_uk.yml",
                "translations/translation_ru.yml", "translations/translation_ru.yml",
                "translations/translation_en.yml", "translations/translation_en.yml"
        ));

        addModuleTranslations();
        getConfig().load(new File(getDataFolder(), "config.yml"));

        WebhookRepositoryHandler repository = new WebhookRepositoryHandler();
        createRepository("webhooks", repository);

        webHookListener = new WebHookListener(this, repository);

        registerSlashCommand(new GithubCommand(repository));

        startServer();

    }

    @Override
    public void onDisable() {

        webHookListener.stopServer();
        getLogger().info("Stopped webhook listener.");

    }


    private void startServer() throws IOException {

        String host = getConfig().getString("webhook-host", "0.0.0.0");
        int port = getConfig().getInt("webhook-port", 8080);

        InetSocketAddress address = new InetSocketAddress(host, port);
        webHookListener.startServer(address);

        getLogger().info("Started webhook listener on [{}:{}]", host, port);

    }

}
