package net.survivalboom.sbds.modules.music.commands;

import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.survivalboom.sbds.api.commands.argument.Argument;
import net.survivalboom.sbds.api.commands.argument.primitive.StringArgument;
import net.survivalboom.sbds.api.commands.base.Command;
import net.survivalboom.sbds.api.commands.base.CommandArgument;
import net.survivalboom.sbds.api.commands.base.CommandBase;
import net.survivalboom.sbds.api.commands.slash.SlashCommand;
import net.survivalboom.sbds.api.commands.slash.SlashExecutionInfo;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.modules.music.bots.BotManager;
import net.survivalboom.sbds.modules.music.bots.GuildPlayer;
import net.survivalboom.sbds.modules.music.bots.MusicBot;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Command(name = "play")
public class PlayCommand extends CommandBase implements SlashCommand {

    private final BotManager botManager;

    public PlayCommand(@NotNull BotManager botManager) {
        this.botManager = botManager;
    }

    @Override
    public void executes(@NotNull SlashExecutionInfo info) {

        Member member = info.guildMember();
        if (member == null) return;

        GuildVoiceState voiceState = member.getVoiceState();
        AudioChannelUnion channel = Objects.requireNonNull(voiceState).getChannel();
        if (channel == null) {
            info.reply("commands.music-module.not-in-voice").queue();
            return;
        }

        String query = info.arguments().get("query", String.class);
        Objects.requireNonNull(query);

        GuildPlayer player = botManager.findCurrentPlayer(channel);
        if (player == null) {

            List<MusicBot> freeBots = botManager.findFreeBots(channel);
            if (freeBots.isEmpty()) {
                info.reply("commands.music-module.no-free-bot").queue();
                return;
            }

            MusicBot bot = freeBots.getFirst();
            player = bot.createPlayer(channel.getGuild());

        }

        List<Track> tracks = player.searchTracks(query);
        if (tracks.isEmpty()) {
            info.reply("commands.music-module.no-tracks-found", Placeholders.of("{QUERY}", query)).queue();
            return;
        }

        boolean newBot = !player.isActive();
        if (newBot) player.connect(channel);

        player.addTracks(tracks);

        Placeholders placeholders = new Placeholders();
        placeholders.add("{BOT}", info.sbds().getBot().getSelfUser().getAsMention());
        placeholders.add("{CHANNEL}", channel.getAsMention());

        if (newBot) {

            Track track = tracks.getFirst();

            placeholders.add("{NAME}", track.getInfo().getSourceName());
            placeholders.add("{DURATION}", track.getInfo().getLength());
            placeholders.add("{COUNT}", tracks.size());

            info.reply("commands.music-module.bot-connected", placeholders).queue();

        }

        else {

            if (tracks.size() > 1) {
                info.reply("commands.music-module.added-tracks-to-playlist", placeholders).queue();
            }

            else {

                Track track = tracks.getFirst();

                placeholders.add("{NAME}", track.getInfo().getSourceName());
                placeholders.add("{DURATION}", track.getInfo().getLength());

                info.reply("commands.music-module.added-to-playlist", placeholders).queue();

            }

        }


    }


    @CommandArgument(name = "query")
    public Argument<?> song() {
        return new StringArgument();
    }

}
