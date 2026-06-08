package net.survivalboom.sbds.core.database.member;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.survivalboom.sbds.api.database.members.IMemberData;
import net.survivalboom.sbds.api.database.members.IMemberDataManager;
import net.survivalboom.sbds.api.utils.container.INamespacedDataContainer;
import net.survivalboom.sbds.api.utils.valid.Valid;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MemberData extends Valid implements IMemberData {

    private final MemberDataManager manager;

    private final MemberDataRecord record;


    private final Member member;


    public MemberData(@NotNull MemberDataRecord record, @NotNull MemberDataManager manager) {

        this.record = record;
        this.manager = manager;

        Guild guild = manager.getSbds().getBot().getGuildById(record.getGuildId());
        Objects.requireNonNull(guild, "guild == null; invalid member data, maybe bot was kicked from that guild?");

        this.member = guild.retrieveMemberById(record.getUserId()).complete();

    }

    @Override
    public @NotNull IMemberDataManager getManager() {
        return manager;
    }

    @Override
    public long getId() {
        return record.getId();
    }


    @Override
    public @NotNull Member getMember() {
        return member;
    }

    @Override
    public @NotNull INamespacedDataContainer container() {
        return record.getContainer();
    }

    // DATABASE //

    public @NotNull MemberDataRecord getRecord() {
        return record;
    }

    @Override
    public void save() {
        manager.save(this);
    }

    @Override
    public @NotNull CompletableFuture<Void> delete() {
        return manager.delete(this);
    }

    //
    // MISC
    //

    @Override
    protected void setValid(boolean v) {
        super.setValid(v);
    }

}
