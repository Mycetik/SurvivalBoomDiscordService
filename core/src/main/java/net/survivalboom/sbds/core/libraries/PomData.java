package net.survivalboom.sbds.core.libraries;

import net.survivalboom.sbds.api.libraries.ArtifactAddress;
import net.survivalboom.sbds.api.libraries.IPomData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;

public class PomData implements IPomData {

    private final String repository;

    private final ArtifactAddress address;

    private final ConfigurationNode data;


    private final List<String> repositories = new ArrayList<>();

    private final Map<String, String> properties = new HashMap<>();

    private final @Nullable IPomData parent;

    private final List<ArtifactAddress> bombSources = new ArrayList<>();

    private final List<ArtifactAddress> bombArtifacts = new ArrayList<>();

    private final List<IPomData> dependencies = new ArrayList<>();


    public PomData(
            @NotNull String repository,
            @NotNull ArtifactAddress address,
            @NotNull ConfigurationNode data,
            @Nullable Collection<String> repositories,
            @Nullable Map<String, String> properties,
            @Nullable IPomData parent,
            @Nullable Collection<ArtifactAddress> bombSources,
            @Nullable Collection<ArtifactAddress> bombArtifacts,
            @Nullable Collection<IPomData> dependencies
    ) {

        Objects.requireNonNull(address, "address == null");
        Objects.requireNonNull(data, "data == null");

        this.repository = repository;
        this.address = address;
        this.data = data;
        this.parent = parent;

        if (properties != null) {
            this.properties.putAll(properties);
        }

        if (repositories != null) {
            this.repositories.addAll(repositories);
        }

        if (bombSources != null) {
            this.bombSources.addAll(bombSources);
        }

        if (bombArtifacts != null) {
            this.bombArtifacts.addAll(bombArtifacts);
        }

        if (dependencies != null) {
            this.dependencies.addAll(dependencies);
        }

    }

    @Override
    public @NotNull String getSourceRepository() {
        return repository;
    }

    @Override
    public @NotNull ArtifactAddress getAddress() {
        return address;
    }

    @Override
    public @NotNull ConfigurationNode getData() {
        return data;
    }

    @Override
    public @NotNull List<String> getDeclaredRepositories() {
        return new ArrayList<>(repositories);
    }

    @Override
    public @Nullable IPomData getParent() {
        return parent;
    }

    @Override
    public @NotNull Map<String, String> getProperties() {
        return new HashMap<>(properties);
    }

    @Override
    public @NotNull List<ArtifactAddress> getBOMbSources() {
        return new ArrayList<>(bombSources);
    }

    @Override
    public @NotNull List<ArtifactAddress> getBOMbArtifacts() {
        return new ArrayList<>(bombArtifacts);
    }

    @Override
    public @NotNull List<IPomData> getDependencies() {
        return new ArrayList<>(dependencies);
    }


    @Override
    public String toString() {
        return String.format(
                "PomData{address=%s, repositories=%s, parent=%s, bombSources=%s, bombArtifacts=%s dependencies=%s}",
                address,
                repositories,
                parent,
                bombSources,
                bombArtifacts,
                dependencies
        );
    }

}
