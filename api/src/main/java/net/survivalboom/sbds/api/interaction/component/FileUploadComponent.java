package net.survivalboom.sbds.api.interaction.component;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class FileUploadComponent extends AbstractLabelComponent<FileUploadComponent.Builder, FileUploadComponent, AttachmentUpload> {

    public FileUploadComponent(
            @NotNull String name,
            @Nullable String title,
            @Nullable String description,
            int row,
            int priority,
            boolean isStatic
    ) {
        super(name, title, description, row, priority, isStatic, Component.Type.FILE_UPLOAD);
    }

    @Override
    public @NotNull FileUploadComponent.Builder copy() {
        return new Builder();
    }

    @Override
    public @NotNull AttachmentUpload createComponent(@NotNull Function<FileUploadComponent, String> componentIdCreator, @NotNull Function<String, String> parser) {
        return AttachmentUpload.create(name).build();
    }

    //
    // BUILDER
    //

    public static class Builder extends AbstractLabelComponent.Builder<Builder, FileUploadComponent, AttachmentUpload> {

        protected Builder() {}

        protected Builder(@NotNull Builder builder) {
            super(builder);
        }

        protected Builder(@NotNull FileUploadComponent component) {
            super(component);
        }

        @Override
        public @NotNull FileUploadComponent build() {
            return new FileUploadComponent(name, title, description, row, priority, isStatic);
        }

        @Override
        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
