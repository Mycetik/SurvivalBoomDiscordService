package net.survivalboom.sbds.modules.voice;

import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.modules.voice.voice.ControlPanelListener;
import net.survivalboom.sbds.modules.voice.voice.VoiceManager;

import java.util.Map;

public class PrivateVoiceModule extends ModuleMain {

    private final VoiceManager voiceManager;

    public PrivateVoiceModule() {
        this.voiceManager = new VoiceManager(this);
    }

    @Override
    public void onEnable() {

        voiceManager.init();

        checkFiles(Map.of(
                "translations/translation_uk.yml", "translations/translation_uk.yml",
                "translations/translation_ru.yml", "translations/translation_ru.yml",
                "translations/translation_en.yml", "translations/translation_en.yml"
        ));
        addModuleTranslations();

        registerEvents(voiceManager);
        registerStringDropdown("action", new ControlPanelListener(voiceManager)::onControlPanelDropdown);

        registerModal("rename", builder ->
            builder
                .setTitle("$[voice.control.rename.modal.title]")
                .addInput(
                        "name",
                        "$[voice.control.set-limit.modal.input-name]",
                        null,
                        "$[voice.control.set-limit.modal.input-placeholder]",
                        null,
                        TextInputStyle.SHORT,
                        3,
                        20,
                        true
                )
        );

        registerModal("limit", builder ->
            builder
                .setTitle("$[voice.control.set-limit.modal.title]")
                .addInput(
                        "limit",
                        "$[voice.control.set-limit.modal.input-name]",
                        null,
                        "$[voice.control.set-limit.modal.input-placeholder]",
                        null,
                        TextInputStyle.SHORT,
                        1,
                        2,
                        true
                )
        );

        createGuildConfig(builder ->
                builder
                    .addField("creator", "voice.config.creator", VoiceChannel.class, null)
                    .addField("fallback", "voice.config.fallback", VoiceChannel.class, null)
        );

    }

    @Override
    public void onDisable() {
        voiceManager.shutdown();
    }


}