package net.survivalboom.sbds.modules.moderation.module.utils;

import net.survivalboom.sbds.api.modules.ModuleMain;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.modules.moderation.module.ModerationModule;
import net.survivalboom.sbds.modules.moderation.module.moderation.AuditManager;
import org.jetbrains.annotations.NotNull;

public abstract class ModerationManager extends Manager {

    protected final ModuleMain module;

    protected final AuditManager auditManager;

    protected final NamespacedKey GUILD_DATA_KEY;


    public ModerationManager(
            @NotNull ModerationModule module
    ) {

        this.module = module;
        this.auditManager = module.getAuditManager();

        GUILD_DATA_KEY = NamespacedKey.fromModule(module, "data");

    }

    @Override
    protected void init0() {}

    @Override
    protected void shutdown0() {}

}
