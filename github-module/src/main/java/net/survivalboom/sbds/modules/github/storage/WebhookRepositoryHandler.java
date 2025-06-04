package net.survivalboom.sbds.modules.github.storage;

import net.survivalboom.sbds.api.database.RepositoryHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WebhookRepositoryHandler extends RepositoryHandler<WebhookData> {

    public WebhookRepositoryHandler() {
        super(WebhookData.class);
    }

    public @Nullable WebhookData getWebhook(int id) {

        WebhookData webhookData = cache.get(id);
        if (webhookData == null) {

            webhookData = sessionReturn(session -> session.get(WebhookData.class, id));

            if (webhookData != null) {
                cache.put(webhookData.id(), webhookData);
            }

        }

        return webhookData;

    }

    public @NotNull WebhookData createWebhook(long channelId) {

        WebhookData webhookData = create(new WebhookData(channelId));

        cache.put(webhookData.id(), webhookData);

        return webhookData;

    }

    public @NotNull List<WebhookData> getWebhooksInChannel(long channel) {

        return sessionReturn(session -> {

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(WebhookData.class);
            var root = query.from(WebhookData.class);

            var channelPredicate = cb.equal(root.get("channelId"), channel);

            query.select(root).where(channelPredicate);

            return session.createQuery(query).getResultList();

        });

    }

    public boolean deleteWebhook(int id) {

        WebhookData webhookData = getWebhook(id);
        if (webhookData == null) return false;

        delete(webhookData);
        cache.remove(id);

        return true;

    }

}
