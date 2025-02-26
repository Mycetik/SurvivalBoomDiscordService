package net.survivalboom.sbds.api.database.users;

import net.survivalboom.sbds.api.translations.ITranslation;
import org.jetbrains.annotations.Nullable;

public interface IUserData {

    long getID();

    @Nullable ITranslation translation();

    void translation(@Nullable ITranslation translation);

}
