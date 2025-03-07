package net.survivalboom.sbds.api.modules;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IModuleMeta {

    @NotNull String getName();

    @NotNull String getVersion();

    @Nullable String getDescription();


    @NotNull String getMain();


    @NotNull List<String> getAuthors();

    @Nullable String getWebsite();


    interface IDependency {

        @NotNull String getName();

        @NotNull LoadOrder getOrder();

        boolean required();

        boolean joinClasspath();

        enum LoadOrder {
            BEFORE,
            AFTER
        }

    }

}
