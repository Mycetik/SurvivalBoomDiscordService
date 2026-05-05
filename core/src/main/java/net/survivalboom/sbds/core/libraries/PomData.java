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

    private final @Nullable IPomData parent;

    private final Map<String, String> properties = new HashMap<>();

    private final List<IPomData> boms = new ArrayList<>();

    private final List<IPomData> dependencies = new ArrayList<>();


    public PomData(
            @NotNull String repository,
            @NotNull ArtifactAddress address,
            @NotNull ConfigurationNode data,
            @Nullable Collection<String> repositories,
            @Nullable IPomData parent,
            @Nullable Map<String, String> properties,
            @Nullable Collection<IPomData> boms,
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

        if (boms != null) {
            this.boms.addAll(boms);
        }

        if (dependencies != null) {
            this.dependencies.addAll(dependencies);
        }

    }

    @Override
    public @NotNull String getRepository() {
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
    public @NotNull List<String> getRepositories() {
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
    public @NotNull List<IPomData> getBOMs() {
        return new ArrayList<>(boms);
    }

    @Override
    public @NotNull List<IPomData> getDependencies() {
        return new ArrayList<>(dependencies);
    }


    @Override
    public String toString() {
        return String.format(
                "PomData{address=%s, repositories=%s, parent=%s, boms=%s, dependencies=%s}",
                address,
                repositories,
                parent,
                boms,
                dependencies
        );
    }

}
