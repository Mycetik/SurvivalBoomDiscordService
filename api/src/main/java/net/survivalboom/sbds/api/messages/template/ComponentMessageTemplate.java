package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComponentMessageTemplate implements IMessageTemplate {


    @Override
    public @NotNull MessageCreateData createMessageData(@Nullable StringParser parser, @Nullable ComponentLinker linker) {
        return null;
    }

    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static @NotNull Builder ofSection(@NotNull ConfigurationSection section) {

    }


    public static class Builder {

        private Builder() {}


        // build //

        public @NotNull ComponentMessageTemplate build() {
            return new ComponentMessageTemplate(sggddf);
        }

    }

}
