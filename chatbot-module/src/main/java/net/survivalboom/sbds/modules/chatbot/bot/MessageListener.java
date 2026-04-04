package net.survivalboom.sbds.modules.chatbot.bot;

import io.github.sashirestela.cleverclient.Event;
import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.common.function.FunctionExecutor;
import io.github.sashirestela.openai.domain.response.Input;
import io.github.sashirestela.openai.domain.response.ResponseRequest;
import io.github.sashirestela.openai.domain.response.ResponseTool;
import io.github.sashirestela.openai.domain.response.stream.EventName;
import io.github.sashirestela.openai.domain.response.stream.ResponseOutputItemEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.Listener;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.modules.chatbot.ChatBotModule;
import net.survivalboom.sbds.modules.chatbot.storage.AIChannels;
import net.survivalboom.sbds.modules.chatbot.utils.MessageBuffer;
import net.survivalboom.sbds.modules.chatbot.utils.functions.DeleteMessage;
import net.survivalboom.sbds.modules.chatbot.utils.functions.MuteFunction;
import net.survivalboom.sbds.modules.chatbot.utils.functions.ReplyMessageFunction;
import net.survivalboom.sbds.modules.chatbot.utils.functions.WarnFunction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class MessageListener extends Manager implements Listener {


    private static final int MAX_CHARACTERS = 2048;


    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

    private final ChatBotModule module;

    private final ChatBot chatBot;

    private final MessageBuffer buffer;

    private final AIChannels channels;


    private final List<ResponseTool> responseTools = new ArrayList<>();

    private final FunctionExecutor functionExecutor;


    public MessageListener(@NotNull ChatBotModule module) {
        this.module = module;
        this.chatBot = module.getChatBot();
        this.channels = chatBot.getChannels();
        this.buffer = new MessageBuffer(this::chatBotReact, module);

        List<FunctionDef> functionDefs = Arrays.asList(
                FunctionDef.of(WarnFunction.class),
                FunctionDef.of(MuteFunction.class),
                FunctionDef.of(ReplyMessageFunction.class),
                FunctionDef.of(DeleteMessage.class)
        );

        this.functionExecutor = new FunctionExecutor(functionDefs);
        this.responseTools.addAll(ResponseTool.FunctionResponseTool.functions(functionDefs));

    }

    @Override
    protected void init0() {
        module.registerEvents(this);
        buffer.init();
    }

    @Override
    protected void shutdown0() {
        buffer.shutdown();
    }


    private void chatBotReact(@NotNull MessageBuffer.Push push) {

        var messages = push.messages();
        var channel = push.channel();

        List<List<Message>> parts = splitMessages(push.messages());

        log.info("[{}:{}] Received push from buffer with {} messages. MPM: {}; Push Interval: {} seconds. Split to {} parts.", channel.getIdLong(), channel.getName(), messages.size(), push.mpm(), push.currentInterval(), parts.size());

        for (var part : parts) {
            var request = createRequest(part, push);
            module.getAiQueue().request(request).thenAccept(this::handleResponseEvents);
        }

    }

    private void handleResponseEvents(Stream<Event> stream) {

        stream.forEach(event -> {

            switch (event.getName()) {

                case EventName.RESPONSE_OUTPUT_ITEM_DONE -> {
                    var item = (ResponseOutputItemEvent) event.getData();
                    var functionCall = (Input.Item.FunctionCallItem) item.getItem();
                    functionExecutor.execute(functionCall.getName(), functionCall.getArguments());
                }

            }

        });

    }


    @EventHandler
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        User user = event.getAuthor();
        if (user.isBot()) {
            return;
        }

        Message message = event.getMessage();
        String content = message.getContentRaw();
        if (content.isBlank()) {
            return;
        }

        MessageChannel channel = event.getChannel();
        if (channel instanceof ThreadChannel) {
            return;
        }

        channels.isAiChannel(channel).thenAccept(v -> {

            if (!v) {
                return;
            }

            buffer.addMessage(message);

        });

    }

    //
    // REQUEST
    //

    private @NotNull List<List<Message>> splitMessages(@NotNull List<Message> in) {

        List<List<Message>> out = new ArrayList<>();

        int charactersCount = 0;
        List<Message> currentList = new ArrayList<>();
        out.add(currentList);

        for (Message message : in) {

            String content = message.getContentRaw();
            charactersCount += content.length();

            if (charactersCount > MAX_CHARACTERS) {
                charactersCount = content.length();
                currentList = new ArrayList<>();
                out.add(currentList);
            }

            currentList.add(message);

        }

        return out;

    }


    private ResponseRequest createRequest(@NotNull List<Message> part, @NotNull MessageBuffer.Push push) {

        String input = prepareInput(part, push);

        return ResponseRequest.builder()
                .instructions(chatBot.getPrompt().replace("{character}", chatBot.getCharacter()))
                .input(input)
                .tools(this.responseTools)
                .model("gpt-4.1-mini")
                .build();

    }

    private String prepareInput(@NotNull List<Message> part, @NotNull MessageBuffer.Push push) {

        StringBuilder builder = new StringBuilder();

        builder
            .append("Тобі наданий список повідомлень із чату. Тобі потрібно їх опрацювати. Повідомлення надаються у форматі: `id_повідомлення [id_користувача:ім'я_користувача]: вміст_повідомлення`")
            .append("\n");

        var channel = push.channel();
        builder
            .append(String.format("Поточний канал: %s (%s)", channel.getId(), channel.getName()))
            .append("\n");

        builder.append("Список повідомлень:\n");

        for (Message message : part) {

            String content = message.getContentRaw();
            if (!message.getAttachments().isEmpty()) {
                content += "[вкладення]";
            }

            builder
                .append(String.format("%s [%s:%s]: %s",
                        message.getId(),
                        message.getMember().getId(),
                        message.getMember().getEffectiveName(),
                        content

                ))
                .append("\n");

        }

        return builder.toString();

    }

}
