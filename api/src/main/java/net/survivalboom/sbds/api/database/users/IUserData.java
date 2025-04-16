package net.survivalboom.sbds.api.database.users;

import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.utils.NamespacedContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IUserData {

    long getID();

    @Nullable ITranslation translation();

    void translation(@Nullable ITranslation translation);

    void save();

    @NotNull NamespacedContainer container();

}
