package net.survivalboom.sbds.modules.github.webhook;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.messages.IMessage;
import net.survivalboom.sbds.api.messages.IMessages;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.api.utils.TypeMap;
import net.survivalboom.sbds.api.utils.configuration.json.JsonConfiguration;
import net.survivalboom.sbds.modules.github.storage.WebhookData;
import net.survivalboom.sbds.modules.github.storage.WebhookRepositoryHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class WebHookListener {

    private static final Logger log = LoggerFactory.getLogger(WebHookListener.class);


    private final WebhookRepositoryHandler webhookRepository;

    private final ISBDS sbds;

    private final ModuleMain module;

    private final IMessages messages;


    private HttpServer server;

    private boolean started = false;

    private int commitNameLength = 0;


    public WebHookListener(@NotNull ModuleMain module, @NotNull WebhookRepositoryHandler webhookRepository) {
        this.webhookRepository = webhookRepository;
        this.sbds = module.getSbds();
        this.module = module;
        this.messages = sbds.getMessages();
    }


    public void startServer(@NotNull InetSocketAddress address) throws IOException {

        if (started) throw new IllegalStateException("started == true");

        commitNameLength = module.getConfig().getInt("commit-name-length", 64);

        server = HttpServer.create(address, 0);
        server.createContext("/", this::handle);

        server.setExecutor(null);
        server.start();

        started = true;

    }

    public void stopServer() {

        if (!started) return;

        started = false;

        server.stop(0);

    }

    private void handle(@NotNull HttpExchange exchange) throws IOException {

        try {

            handle0(exchange);

        }

        catch (Throwable t) {
            log.error("Webhook listener error.", t);
            exchange.sendResponseHeaders(500, -1);
        }

    }

    private void handle0(@NotNull HttpExchange exchange) throws Throwable {

        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        int id = getId(exchange.getRequestURI().getPath().substring(1));
        if (id == -1) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        WebhookData webhook = webhookRepository.getWebhook(id);
        if (webhook == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String eventType = exchange.getRequestHeaders().getFirst("X-GitHub-Event");
        if (eventType == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        String str = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonConfiguration json = new JsonConfiguration();
        json.loadFromString(str);

        if (eventType.equals("repository")) {
            eventType = json.getString("action", "null");
        }

        GuildMessageChannel channel = webhook.channel(sbds.getBot());
        MessageCreateData messageCreateData = switch (eventType) {

            case "push" -> createPushMessage(channel, json);

            case "deleted" -> createSimpleMessage(channel, json, "github.notifications.delete");

            case "created" -> createSimpleMessage(channel, json, "github.notifications.create");

            default -> null;

        };

        if (messageCreateData == null) {
            exchange.sendResponseHeaders(418, -1);
            return;
        }

        channel.sendMessage(messageCreateData).queue();

        exchange.sendResponseHeaders(200, -1);

    }

    private int getId(@NotNull String raw) {

        try {
            return Integer.parseInt(raw);
        }

        catch (NumberFormatException e) {
            return -1;
        }

    }

    @SuppressWarnings("unchecked")
    private MessageCreateData createPushMessage(@NotNull GuildMessageChannel channel, @NotNull JsonConfiguration json) {

        String repositoryFullname = json.getString("repository.full_name");

        String pusherUsername = json.getString("sender.login");
        String pusherProfileUrl = json.getString("sender.html_url");
        String pusherAvatarUrl = json.getString("sender.avatar_url");

        String branch = json.getString("ref", "refs/heads/null").substring(11);

        StringBuilder builder = new StringBuilder();
        int commits = 0;
        for (TypeMap map : TypeMap.ofMapList(json.getMapList("commits"))) {

            String message = map.getCastOrDefault("message", String.class, "null");
            int index = message.indexOf("\n");
            if (index != -1) message = message.substring(0, index);

            if (message.length() >= commitNameLength) {
                message = message.substring(0, commitNameLength) + "...";
            }

            List<String> added = map.getCastOrNull("added", List.class);
            List<String> removed = map.getCastOrNull("removed", List.class);
            List<String> modified = map.getCastOrNull("modified", List.class);

            Objects.requireNonNull(added, "added == null");
            Objects.requireNonNull(removed, "removed == null");
            Objects.requireNonNull(modified, "modified == null");

            int files = added.size() + removed.size() + modified.size();

            builder.append(commitMessage(Placeholders.of("{FILES}", files, "{MESSAGE}", message), channel));

            commits++;

        }

        Placeholders placeholders = new Placeholders();
        placeholders.add("{COMMITS-COUNT}", commits)
                    .add("{BRANCH}", branch)
                    .add("{REPOSITORY}", repositoryFullname)
                    .add("{COMMITS}", builder)
                    .add("{AUTHOR}", pusherUsername)
                    .add("{AUTHOR-PROFILE}", pusherProfileUrl)
                    .add("{AUTHOR-AVATAR}", pusherAvatarUrl);


        return messages.createMessageBuilder("github.notifications.push", channel.getGuild()).withPlaceholders(placeholders).build();

    }

    private MessageCreateData createSimpleMessage(@NotNull GuildMessageChannel channel, @NotNull JsonConfiguration json, @NotNull String key) {

        String repositoryFullname = json.getString("repository.full_name");

        String pusherUsername = json.getString("sender.login");
        String pusherProfileUrl = json.getString("sender.html_url");
        String pusherAvatarUrl = json.getString("sender.avatar_url");

        Placeholders placeholders = new Placeholders();
        placeholders
                .add("{REPOSITORY}", repositoryFullname)
                .add("{AUTHOR}", pusherUsername)
                .add("{AUTHOR-PROFILE}", pusherProfileUrl)
                .add("{AUTHOR-AVATAR}", pusherAvatarUrl);


        return messages.createMessageBuilder(key, channel.getGuild()).withPlaceholders(placeholders).build();

    }

    private @NotNull String commitMessage(@NotNull Placeholders placeholders, @NotNull GuildMessageChannel channel) {
        IMessage message = messages.getMessage("github.commit-format", channel.getGuild(), true);
        if (message == null) return "";
        return message.buildString(placeholders);
    }

}
