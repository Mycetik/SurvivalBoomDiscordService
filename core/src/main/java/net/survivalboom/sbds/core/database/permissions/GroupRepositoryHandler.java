package net.survivalboom.sbds.core.database.permissions;

import jakarta.persistence.Query;
import net.survivalboom.sbds.api.database.RepositoryHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class GroupRepositoryHandler extends RepositoryHandler<GroupRecord> {

    public GroupRepositoryHandler() {
        super(GroupRecord.class);
    }


    public @NotNull List<GroupRecord> getGuildGroups(long guildId) {

        List<GroupRecord> out = cache.values().stream().filter(gd -> gd.guildId() == guildId).toList();
        if (!out.isEmpty()) return out;

        out = sessionReturn(session -> {

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(GroupRecord.class);
            var root = query.from(GroupRecord.class);

            var guildIdPredicate = cb.equal(root.get("guildId"), guildId);

            query.select(root).where(guildIdPredicate);

            return session.createQuery(query).getResultList();

        });

        out.forEach(gd -> cache.put(Objects.hash(gd.id(), gd.guildId()), gd));

        return out;

    }

    public @NotNull GroupRecord createGuildGroup(long guildId, @NotNull String group) {

        GroupRecord record = create(new GroupRecord(guildId, group));

        cache.put(Objects.hash(record.id(), record.guildId()), record);

        return record;

    }

    public void removeGuildGroup(long guildId, @NotNull String group) {

        session(session -> {

            session.beginTransaction();

            Query query = session.createQuery("DELETE FROM GroupRecord g WHERE g.guildId = :guildId AND g.name = :name");
            query.setParameter("guildId", guildId);
            query.setParameter("name", group);

            session.getTransaction().commit();

        });

    }


}
