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
        return getById(id).join();
    }

    public @NotNull WebhookData createWebhook(long channelId) {
        return save(new WebhookData(channelId)).join();
    }

    public @NotNull List<WebhookData> getWebhooksInChannel(long channel) {

        return sessionReturn(session -> {

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(WebhookData.class);
            var root = query.from(WebhookData.class);

            var channelPredicate = cb.equal(root.get("channelId"), channel);

            query.select(root).where(channelPredicate);

            return session.createQuery(query).getResultList();

        }, true).join();

    }

    public void deleteWebhook(int id) {
        delete(id);
    }

}
