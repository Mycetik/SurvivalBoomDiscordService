package net.survivalboom.sbds.modules.ai;

import io.github.sashirestela.cleverclient.client.OkHttpClientAdapter;
import io.github.sashirestela.openai.SimpleOpenAI;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;

public class AIModule extends ModuleMain {

    private SimpleOpenAI manager;

    @Override
    public void onEnable() throws Throwable {

        File keyFile = new File(getDataFolder(), "openai-token");
        getDataFolder().mkdirs();

        if (!keyFile.exists()) {
            getLogger().error("Token file does not exist. Please provide an OpenAI token in `{}`.", keyFile.getAbsolutePath());
            keyFile.createNewFile();
            getModuleManager().disableModule(this);
            return;
        }

        if (!keyFile.isFile()) {
            getLogger().error("Invalid token file. Please provide an OpenAI token in `{}`.", keyFile.getAbsolutePath());
            getModuleManager().disableModule(this);
            return;
        }

        String token;
        try (FileInputStream stream = new FileInputStream(keyFile)) {
            token = new String(stream.readAllBytes());
        }

        if (token.isBlank()) {
            getLogger().error("Token is empty. Please provide an OpenAI token in `{}`.", keyFile.getAbsolutePath());
            getModuleManager().disableModule(this);
            return;
        }

        manager = SimpleOpenAI.builder()
                .apiKey(token)
                .clientAdapter(new OkHttpClientAdapter())
                .build();

        registerService(manager);

    }

    @Override
    public void onDisable() {
        manager = null;
    }


    public @NotNull SimpleOpenAI getManager() {
        return manager;
    }

}
