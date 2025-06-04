package net.survivalboom.sbds.modules.github.storage;

import jakarta.persistence.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.survivalboom.sbds.api.database.DataRecord;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Entity
@Table(name = "github_webhooks")
public class WebhookData extends DataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private long channelId;

    public WebhookData() {}

    public WebhookData(long channelId) {
        this.channelId = channelId;
    }


    public int id() {
        return id;
    }

    public long channelId() {
        return channelId;
    }

    public @NotNull GuildMessageChannel channel(@NotNull JDA jda) {
        return Objects.requireNonNull(jda.getChannelById(GuildMessageChannel.class, channelId));
    }

}
