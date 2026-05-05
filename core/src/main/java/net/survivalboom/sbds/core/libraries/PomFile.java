package net.survivalboom.sbds.core.libraries;

import java.util.Objects;

public record PomFile(

) {

    public PomFile {

        Objects.requireNonNull(info, "info == null");
        Objects.requireNonNull(pom, "pom == null");
        Objects.requireNonNull(url, "url == null");

    }

}
