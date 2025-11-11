package net.survivalboom.sbds.moderation.module;

import net.dv8tion.jda.api.entities.User;
import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.moderation.api.IModerationModule;
import net.survivalboom.sbds.moderation.api.moderation.*;
import net.survivalboom.sbds.moderation.api.storage.IAuditEntry;
import net.survivalboom.sbds.moderation.api.storage.IPunishmentData;
import net.survivalboom.sbds.moderation.module.commands.ban.BanCommand;
import net.survivalboom.sbds.moderation.module.commands.ban.UnBanCommand;
import net.survivalboom.sbds.moderation.module.commands.kick.KickCommand;
import net.survivalboom.sbds.moderation.module.commands.mute.MuteCommand;
import net.survivalboom.sbds.moderation.module.commands.mute.UnMuteCommand;
import net.survivalboom.sbds.moderation.module.commands.warn.UnWarnCommand;
import net.survivalboom.sbds.moderation.module.commands.warn.WarnCommand;
import net.survivalboom.sbds.moderation.module.moderation.*;
import net.survivalboom.sbds.moderation.module.storage.records.Ban;
import net.survivalboom.sbds.moderation.module.storage.records.Mute;
import net.survivalboom.sbds.moderation.module.storage.records.Warn;
import net.survivalboom.sbds.moderation.module.utils.ExpiringQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModerationModule extends ModuleMain implements IModerationModule {


    private AuditManager auditManager;

    private ExpiringQueue expiringQueue;


    private BanManager banManager;

    private KickManager kickManager;

    private MuteManager muteManager;

    private WarnManager warnManager;


    @Override
    public void onLoad() throws Throwable {

        this.auditManager = new AuditManager(this);
        this.expiringQueue = new ExpiringQueue(this);

        this.banManager = new BanManager(expiringQueue, this);
        this.kickManager = new KickManager(this);
        this.muteManager = new MuteManager(expiringQueue, this);
        this.warnManager = new WarnManager(expiringQueue, this);

    }

    @Override
    public void onUnload() {

        this.auditManager = null;
        this.expiringQueue = null;

        this.banManager = null;
        this.kickManager = null;
        this.muteManager = null;
        this.warnManager = null;

    }

    @Override
    public void onEnable() {

        checkFiles(Map.of(
                "translations/translation_uk.yml", "translations/translation_uk.yml"
        ));

        addModuleTranslations();


        // managers //

        auditManager.init();
        expiringQueue.init();

        kickManager.init();
        banManager.init();
        muteManager.init();
        warnManager.init();


        // ban //
        registerCommand(new BanCommand(banManager));
        registerCommand(new UnBanCommand(banManager));

        // mute //
        registerCommand(new MuteCommand(muteManager));
        registerCommand(new UnMuteCommand(muteManager));

        // warn //
        registerCommand(new WarnCommand(warnManager));
        registerCommand(new UnWarnCommand(warnManager));

        // kick //
        registerCommand(new KickCommand(kickManager));


        // service provider //
        registerService(this);

    }

    @Override
    public void onDisable() {

        kickManager.shutdown();
        banManager.shutdown();
        muteManager.shutdown();
        warnManager.shutdown();

        expiringQueue.shutdown();
        auditManager.shutdown();

    }

    //
    // API
    //

    @Override
    public @NotNull AuditManager getAuditManager() {
        return auditManager;
    }

    @Override
    public @NotNull IBanManager getBanManager() {
        return banManager;
    }

    @Override
    public @NotNull IKickManager getKickManager() {
        return kickManager;
    }

    @Override
    public @NotNull IMuteManager getMuteManager() {
        return muteManager;
    }

    @Override
    public @NotNull IWarnManager getWarnManager() {
        return warnManager;
    }


    @Override
    public @NotNull CompletableFuture<@NotNull IAuditEntry> removePunishment(
            @NotNull IPunishmentData punishment,
            @Nullable User responsible,
            @Nullable String reason,
            @Nullable String comment
    ) {

        return switch (punishment) {

            case Ban ban -> banManager.removeBan(ban, responsible, reason, comment);

            case Mute mute -> muteManager.removeMute(mute, responsible, reason, comment);

            case Warn warn -> warnManager.removeWarn(warn, responsible, reason, comment);

            default -> throw new IllegalArgumentException("Invalid object");

        };

    }

}
