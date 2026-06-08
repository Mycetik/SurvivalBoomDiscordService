package net.survivalboom.sbds.core.database.users;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.database.users.IUserDataManager;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.container.INamespacedDataContainer;
import net.survivalboom.sbds.api.utils.valid.Valid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class UserData extends Valid implements IUserData {

    private final UserDataManager manager;

    private final UserDataRecord record;


    private final User user;


    public UserData(@NotNull UserDataRecord record, @NotNull UserDataManager manager) {

        this.record = record;
        this.manager = manager;

        this.user = manager.getSbds().getBot().retrieveUserById(record.getUserId()).complete();

    }

    @Override
    public @NotNull IUserDataManager getManager() {
        return manager;
    }

    // USER //

    @Override
    public @NotNull User getUser() {
        return user;
    }

    // TRANSLATION //

    @Override
    public @Nullable ITranslation getTranslation() {
        return record.getTranslation();
    }

    @Override
    public void setTranslation(@Nullable ITranslation translation) {
        checkValid();
        record.setTranslation(translation);
        save();
    }

    // DATABASE //

    public UserDataRecord getRecord() {
        return record;
    }

    @Override
    public @NotNull INamespacedDataContainer container() {
        checkValid();
        return record.getContainer();
    }

    @Override
    public void save() {
        manager.save(this);
    }

    @Override
    public @NotNull CompletableFuture<Void> delete() {
        return manager.delete(this);
    }

    //
    // MISC
    //

    @Override
    protected void setValid(boolean v) {
        super.setValid(v);
    }

}
