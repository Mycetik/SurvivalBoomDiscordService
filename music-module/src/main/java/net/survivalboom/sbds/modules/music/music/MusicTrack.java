package net.survivalboom.sbds.modules.music.music;

import dev.arbjerg.lavalink.client.player.Track;
import net.survivalboom.sbds.api.utils.placeholders.IPlaceholders;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MusicTrack implements IPlaceholders {

    private final Track track;


    public MusicTrack(@NotNull Track track) {
        this.track = track;
    }


    public @NotNull Track getTrack() {
        return track;
    }

    public @NotNull String getTitle() {
        return track.getInfo().getTitle();
    }

    public @NotNull String getSource() {
        return track.getInfo().getSourceName();
    }

    public @NotNull String getLink() {
        return Objects.requireNonNull(track.getInfo().getUri());
    }

    public long getDuration() {
        return track.getInfo().getLength();
    }

    public @NotNull String getDurationFormated() {
        return formatTime(getDuration());
    }


    private static String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }


    @Override
    public @NotNull Placeholders placeholders() {
        return Placeholders.of(
                "title", getTitle(),
                "source", getSource(),
                "link", getLink(),
                "duration", getDurationFormated()
        );
    }

}
