package net.survivalboom.sbds.moderation.module;

import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.moderation.api.IModerationModule;
import net.survivalboom.sbds.moderation.module.commands.ban.BanCommand;
import net.survivalboom.sbds.moderation.module.commands.ban.UnBanCommand;
import net.survivalboom.sbds.moderation.module.commands.mute.MuteCommand;
import net.survivalboom.sbds.moderation.module.commands.mute.UnMuteCommand;
import net.survivalboom.sbds.moderation.module.moderation.ModerationManager;

import java.util.Map;

public class ModerationModule extends ModuleMain implements IModerationModule {

    private ModerationManager moderationManager;


    @Override
    public void onEnable() {

        checkFiles(Map.of(
                "translations/translation_uk.yml", "translations/translation_uk.yml"
        ));

        addModuleTranslations();

        getLogger().info("Loading moderation manager...");

        moderationManager = new ModerationManager(this);
        moderationManager.init();

        // ban //
        registerCommand(new BanCommand(moderationManager));
        registerCommand(new UnBanCommand(moderationManager));

        // mute //
        registerCommand(new MuteCommand(moderationManager));
        registerCommand(new UnMuteCommand(moderationManager));

    }

    @Override
    public void onDisable() {

        getLogger().info("Shutting down moderation manager...");

        moderationManager.shutdown();
        moderationManager = null;

    }

}
