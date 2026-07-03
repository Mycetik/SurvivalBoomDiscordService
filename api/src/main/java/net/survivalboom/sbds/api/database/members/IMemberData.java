package net.survivalboom.sbds.api.database.members;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.utils.container.INamespacedDataContainer;
import net.survivalboom.sbds.api.utils.valid.IValid;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface IMemberData extends IValid {

    @NotNull IMemberDataManager getManager();

    // MEMBER //

    long getId();

    @NotNull Member getMember();

    default @NotNull Guild getGuild() {
        return getMember().getGuild();
    }

    default @NotNull User getUser() {
        return getMember().getUser();
    }

    // DATA //

    @NotNull INamespacedDataContainer container();

    void save();

    @NotNull CompletableFuture<Void> delete();

}
