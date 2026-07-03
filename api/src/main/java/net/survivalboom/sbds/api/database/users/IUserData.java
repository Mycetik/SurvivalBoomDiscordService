package net.survivalboom.sbds.api.database.users;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.utils.container.INamespacedDataContainer;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.valid.IValid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface IUserData extends IValid {

    @NotNull IUserDataManager getManager();

    // USER //

    @NotNull User getUser();

    // TRANSLATION //

    @Nullable ITranslation getTranslation();

    void setTranslation(@Nullable ITranslation translation);

    // DATA //

    @NotNull INamespacedDataContainer container();

    void save();

    @NotNull CompletableFuture<Void> delete();

}
