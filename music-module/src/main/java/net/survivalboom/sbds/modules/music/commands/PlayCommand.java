package net.survivalboom.sbds.modules.music.commands;

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
import net.survivalboom.sbds.modules.music.bots.TrackLoadException;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
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

        //
        // FIND PLAYER
        //

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

        //
        // ADD TRACKS
        //

        boolean isUrl = isUrl(query);

        if (isUrl) info.reply("commands.music-module.loading-tracks").withPlaceholders(Placeholders.of("{URL}", query)).queue();
        else info.reply("commands.music-module.searching-tracks").withPlaceholders(Placeholders.of("{QUERY}", query)).queue();

        List<Track> tracks;
        try {
            tracks = player.searchTracks(query);
        }

        catch (TrackLoadException e) {
            info.reply("commands.music-module.track-failed").withPlaceholders(Placeholders.of("{ERROR}", e.toString())).queue();
            return;
        }

        if (tracks.isEmpty()) {
            info.edit("commands.music-module.no-tracks-found").withPlaceholders(Placeholders.of("{QUERY}", query)).queue();
            return;
        }

        boolean newBot = !player.isActive();
        if (newBot) player.connect(channel);

        player.addTracks(tracks);

        //
        // SEARCH TRACKS
        //

        Placeholders placeholders = new Placeholders();
        placeholders.add("{BOT}", info.sbds().getBot().getSelfUser().getAsMention());
        placeholders.add("{CHANNEL}", channel.getAsMention());

        if (newBot) {

            Track track = tracks.getFirst();

            placeholders.add("{NAME}", track.getInfo().getSourceName());
            placeholders.add("{DURATION}", track.getInfo().getLength());
            placeholders.add("{COUNT}", tracks.size());

            info.edit("commands.music-module.bot-connected").withPlaceholders(placeholders).queue();

        }

        else {

            if (tracks.size() > 1) {
                info.edit("commands.music-module.added-tracks-to-playlist").withPlaceholders(placeholders).queue();
            }

            else {

                Track track = tracks.getFirst();

                placeholders.add("{NAME}", track.getInfo().getSourceName());
                placeholders.add("{DURATION}", track.getInfo().getLength());

                info.edit("commands.music-module.added-to-playlist").withPlaceholders(placeholders).queue();

            }

        }


    }

    private boolean isUrl(@NotNull String string) {

        try {
            new URI(string);
            return true;
        }

        catch (URISyntaxException e) {
            return false;
        }

    }

    @CommandArgument(name = "query")
    public Argument<?> song() {
        return new StringArgument();
    }

}
