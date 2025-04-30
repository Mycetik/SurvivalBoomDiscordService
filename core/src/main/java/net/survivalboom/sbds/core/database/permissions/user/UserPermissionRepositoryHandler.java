package net.survivalboom.sbds.core.database.permissions.user;

import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.api.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserPermissionRepositoryHandler extends RepositoryHandler<UserPermissionData> {

    public UserPermissionRepositoryHandler() {
        super(UserPermissionData.class);
    }

    public @NotNull Set<Permission> getUserPermissions(long guildId, long userId) {

        return sessionReturn(session -> {

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(UserPermissionData.class);
            var root = query.from(UserPermissionData.class);

            var guildIdPredicate = cb.equal(root.get("guildId"), guildId);
            var userIdPredicate = cb.equal(root.get("userId"), userId);

            query.select(root).where(cb.and(guildIdPredicate, userIdPredicate));

            List<UserPermissionData> result = session.createQuery(query).getResultList();

            return result.stream().map(UserPermissionData::toPermission).collect(Collectors.toSet());

        });

    }

}
