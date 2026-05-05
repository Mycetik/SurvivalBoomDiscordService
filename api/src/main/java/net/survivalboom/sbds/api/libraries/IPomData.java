package net.survivalboom.sbds.api.libraries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;
import java.util.Map;

public interface IPomData {

    // INFO //

    @NotNull String getRepository();

    @NotNull ArtifactAddress getAddress();

    @NotNull ConfigurationNode getData();

    // DATA //

    @NotNull List<String> getRepositories();

    @Nullable IPomData getParent();

    @NotNull Map<String, String> getProperties();

    @NotNull List<IPomData> getBOMs();

    @NotNull List<IPomData> getDependencies();

}
