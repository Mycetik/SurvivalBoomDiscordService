package net.survivalboom.sbds.api.libraries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;
import java.util.Map;

public interface IPomData {

    // INFO //

    @NotNull String getSourceRepository();

    @NotNull ArtifactAddress getAddress();

    @NotNull ConfigurationNode getData();

    // DATA //

    @NotNull List<String> getDeclaredRepositories();

    @NotNull Map<String, String> getProperties();

    @Nullable IPomData getParent();

    @NotNull List<IPomData> getBOMbSources();

    @NotNull List<ArtifactAddress> getBOMbArtifacts();

    @NotNull List<IPomData> getDependencies();

}
