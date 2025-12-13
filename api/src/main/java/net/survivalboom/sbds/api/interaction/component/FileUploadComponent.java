package net.survivalboom.sbds.api.interaction.component;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class FileUploadComponent extends AbstractLabelComponent<FileUploadComponent.Builder, FileUploadComponent, AttachmentUpload> {

    protected final boolean required;

    public FileUploadComponent(
            @NotNull String name,
            @Nullable String title,
            @Nullable String description,
            boolean required,
            int row,
            int priority,
            boolean isStatic
    ) {
        super(name, title, description, row, priority, isStatic, Component.Type.FILE_UPLOAD);
        this.required = required;
    }

    @Override
    public @NotNull FileUploadComponent.Builder copy() {
        return new Builder();
    }

    @Override
    public @NotNull AttachmentUpload createComponent(@NotNull Function<String, String> parser, @Nullable Function<IComponent, String> componentIdCreator) {
        return AttachmentUpload.create(name).setRequired(required).build();
    }

    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractLabelComponent.Builder<Builder, FileUploadComponent, AttachmentUpload> {

        protected boolean required = true;

        protected Builder() {}

        protected Builder(@NotNull Builder builder) {
            super(builder);
            this.required = builder.required;
        }

        protected Builder(@NotNull FileUploadComponent component) {
            super(component);
            this.required = component.required;
        }

        // REQUIRED //

        public @NotNull Builder setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public boolean isRequired() {
            return required;
        }

        // BUILD //

        @Override
        public @NotNull FileUploadComponent build() {
            return new FileUploadComponent(name, title, description, required, row, priority, isStatic);
        }

        @Override
        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
