package net.survivalboom.sbds.modules.translation.listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.database.users.IUserDataManager;
import net.survivalboom.sbds.api.events.EventHandler;
import net.survivalboom.sbds.api.events.EventListener;
import net.survivalboom.sbds.api.events.EventPriority;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.translations.ITranslationManager;
import net.survivalboom.sbds.modules.translation.TranslationModule;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class SlashCommandListener implements EventListener {

    private final Logger logger;

    private final ITranslationManager translationManager;

    private final IUserDataManager repository;

    public SlashCommandListener(@NotNull TranslationModule module) {
        this.translationManager = module.getSbds().getTranslationManager();
        this.repository = module.getSbds().getUserDataManager();
        this.logger = module.getLogger();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSlashCommand(SlashCommandInteractionEvent event) {

        IUserData userData = repository.obtain(event.getUser()).join();
        if (userData.getTranslation() != null) {
            return;
        }

        DiscordLocale locale = event.getUserLocale();
        ITranslation translation = translationManager.findTranslationByLocale(locale);
        if (translation == null) {
            return;
        }

        userData.setTranslation(translation);
        userData.save();

        logger.info("Successfully set `{}` for `{}` based on user's discord locale `{}`.", translation.getName(), event.getUser().getEffectiveName(), locale);

    }

}
