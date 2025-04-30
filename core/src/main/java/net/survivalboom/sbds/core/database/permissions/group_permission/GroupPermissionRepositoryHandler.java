package net.survivalboom.sbds.core.database.permissions.group_permission;

import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.api.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupPermissionRepositoryHandler extends RepositoryHandler<GroupPermissionData> {

    public GroupPermissionRepositoryHandler() {
        super(GroupPermissionData.class);
    }

    public @NotNull Set<Permission> getGroupPermissions(long guildId, @NotNull String group) {

        return sessionReturn(session -> {

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(GroupPermissionData.class);
            var root = query.from(GroupPermissionData.class);

            var guildIdPredicate = cb.equal(root.get("guildId"), guildId);
            var userIdPredicate = cb.equal(root.get("group"), group);

            query.select(root).where(cb.and(guildIdPredicate, userIdPredicate));

            List<GroupPermissionData> result = session.createQuery(query).getResultList();

            return result.stream().map(GroupPermissionData::toPermission).collect(Collectors.toSet());

        });

    }

}
