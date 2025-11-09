package net.survivalboom.sbds.core.database.permissions;

import net.survivalboom.sbds.api.database.RepositoryHandler;
import net.survivalboom.sbds.api.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserPermissionRepositoryHandler extends RepositoryHandler<UserPermissionRecord> {

    public UserPermissionRepositoryHandler() {
        super(UserPermissionRecord.class);
    }

    public @NotNull Set<Permission> getUserPermissions(long guildId, long userId) {

        return sessionReturn(session -> {

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(UserPermissionRecord.class);
            var root = query.from(UserPermissionRecord.class);

            var guildIdPredicate = cb.equal(root.get("guildId"), guildId);
            var userIdPredicate = cb.equal(root.get("userId"), userId);

            query.select(root).where(cb.and(guildIdPredicate, userIdPredicate));

            List<UserPermissionRecord> result = session.createQuery(query).getResultList();

            return result.stream().map(UserPermissionRecord::toPermission).collect(Collectors.toSet());

        }, false).join();

    }

}
