package net.survivalboom.sbds.modules.chatbot.ai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.*;
import net.survivalboom.sbds.api.utils.Manager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class OpenAiManager extends Manager {

    private static final Logger log = LoggerFactory.getLogger(OpenAiManager.class);
    private final File keyFile;

    private OpenAIClient client;

    private boolean enabled;


    public OpenAiManager(@NotNull File file) {
        this.keyFile = file;
    }


    @Override
    protected void init0() {

        enabled = keyFile.exists();
        if (!enabled) {

            try {
                keyFile.createNewFile();
            }

            catch (IOException e) {
                throw new RuntimeException(e);
            }

            log.error("OpenAI token not provided. Please provide an OpenAI token in `{}`", keyFile.getAbsolutePath());
            return;
        }

        String token;
        try {
            token = readToken();
        }

        catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (token.isBlank()) {
            log.error("OpenAI token not provided. Please provide an OpenAI token in `{}`", keyFile.getAbsolutePath());
            enabled = false;
            return;
        }

        client = OpenAIOkHttpClient.builder().apiKey(token).build();

    }

    @Override
    protected void shutdown0() {
        if (client == null) return;
        client.close();
        client = null;
    }


    private @NotNull String readToken() throws IOException {

        try (FileInputStream stream = new FileInputStream(keyFile)) {
            return new String(stream.readAllBytes());
        }

    }


    public @NotNull ChatCompletion chatCompletion(@NotNull List<ChatCompletionMessageParam> messages, @NotNull ChatModel model) {

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .messages(messages)
                .model(model)
                .build();

        return client.chat().completions().create(params);

    }

    public boolean isEnabled() {
        return enabled;
    }

}
