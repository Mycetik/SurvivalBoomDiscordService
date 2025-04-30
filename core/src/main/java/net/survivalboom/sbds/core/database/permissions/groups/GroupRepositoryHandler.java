package net.survivalboom.sbds.core.database.permissions.groups;

import net.survivalboom.sbds.api.database.RepositoryHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class GroupRepositoryHandler extends RepositoryHandler<GroupData> {

    public GroupRepositoryHandler() {
        super(GroupData.class);
    }

    public @NotNull List<GroupData> getGuildGroups(long guildId) {

        List<GroupData> out = cache.values().stream().filter(gd -> gd.guildId() == guildId).toList();
        if (!out.isEmpty()) return out;

        out = sessionReturn(session -> {

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(GroupData.class);
            var root = query.from(GroupData.class);

            var guildIdPredicate = cb.equal(root.get("guildId"), guildId);

            query.select(root).where(guildIdPredicate);

            return session.createQuery(query).getResultList();

        });

        out.forEach(gd -> cache.put(Objects.hash(gd.id(), gd.guildId()), gd));

        return out;

    }

}
