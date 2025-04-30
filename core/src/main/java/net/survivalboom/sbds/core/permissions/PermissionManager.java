package net.survivalboom.sbds.core.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.survivalboom.sbds.api.database.guilds.IGuildData;
import net.survivalboom.sbds.api.database.users.IUserData;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.permissions.IPermissionManager;
import net.survivalboom.sbds.api.permissions.Permission;
import net.survivalboom.sbds.api.utils.Manager;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.database.Database;
import net.survivalboom.sbds.core.database.permissions.group_permission.GroupPermissionRepositoryHandler;
import net.survivalboom.sbds.core.database.permissions.groups.GroupData;
import net.survivalboom.sbds.core.database.permissions.groups.GroupRepositoryHandler;
import net.survivalboom.sbds.core.database.permissions.user.UserPermissionRepositoryHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class PermissionManager extends Manager implements IPermissionManager {

    private static final Logger log = LoggerFactory.getLogger("PermissionManager");

    private final SBDS sbds;


    private final Map<Integer, Group> cachedGroups = new HashMap<>();

    private final Map<Integer, PermissionUser> cachedUsers = new HashMap<>();


    private UserPermissionRepositoryHandler userPermissionRepository;

    private GroupPermissionRepositoryHandler groupPermissionRepository;

    private GroupRepositoryHandler groupRepository;

    public PermissionManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
    }

    @Override
    protected void init0() {

        userPermissionRepository = new UserPermissionRepositoryHandler();
        groupPermissionRepository = new GroupPermissionRepositoryHandler();
        groupRepository = new GroupRepositoryHandler();

        Database database = sbds.getDatabase();
        database.createRepository0(null, NamespacedKey.sbds("user_permissions"), userPermissionRepository, false);
        database.createRepository0(null, NamespacedKey.sbds("group_permissions"), groupPermissionRepository, false);
        database.createRepository0(null, NamespacedKey.sbds("permission_groups"), groupRepository, true);

    }

    @Override
    protected void shutdown0() {

    }

    @Override
    public boolean hasPermission(long guildId, long userId, @NotNull String permission) {
        Objects.requireNonNull(permission, "permission == null");
        checkValid();

        PermissionUser user = createPermissionUser(guildId, userId);

        return user.hasPermission(permission);

    }

    @Override
    public boolean hasPermission(@NotNull Guild guild, @NotNull Member member, @NotNull String permission) {
        Objects.requireNonNull(guild, "guild == null");
        Objects.requireNonNull(member, "user == null");
        return hasPermission(guild.getIdLong(), member.getIdLong(), permission);
    }

    @Override
    public boolean hasPermission(@NotNull IGuildData guild, @NotNull IUserData user, @NotNull String permission) {
        Objects.requireNonNull(guild, "guild == null");
        Objects.requireNonNull(user, "user == null");
        return hasPermission(guild.getId(), user.getID(), permission);
    }


    @Override
    public void setUserPermission(long guildId, long userId, @NotNull String permission, boolean value) {
        setUserPermission(guildId, userId, new Permission(permission, value));
    }

    @Override
    public void setUserPermission(long guildId, long userId, @NotNull Permission permission) {

        PermissionUser user = createPermissionUser(guildId, userId);
        user.addUserPermission(permission);

        user.rebuildPermissionCache();

    }

    @Override
    public void setUserPermissions(long guildId, long userId, @NotNull Set<Permission> permissions) {

        PermissionUser user = createPermissionUser(guildId, userId);
        permissions.forEach(user::addUserPermission);

        user.rebuildPermissionCache();

    }


    @Override
    public void unsetUserPermission(long guildId, long userId, @NotNull Permission permission) {
        unsetUserPermission(guildId, userId, permission.permission());
    }

    @Override
    public void unsetUserPermission(long guildId, long userId, @NotNull String permission) {

        if (!cachedUsers.containsKey(Objects.hash(guildId, userId))) return;

        PermissionUser user = createPermissionUser(guildId, userId);
        user.unsetUserPermission(permission);

        user.rebuildPermissionCache();

    }

    @Override
    public void unsetUserPermissions(long guildId, long userId, @NotNull Set<Permission> permissions) {

        if (!cachedUsers.containsKey(Objects.hash(guildId, userId))) return;

        PermissionUser user = createPermissionUser(guildId, userId);
        permissions.forEach(p -> user.unsetUserPermission(p.permission()));

        user.rebuildPermissionCache();

    }


    @Override
    public void setGroupPermission(long guildId, @NotNull String group, @NotNull String permission, boolean value) {

    }

    @Override
    public void setGroupPermission(long guildId, @NotNull String group, @NotNull Permission permission) {

    }

    @Override
    public void setGroupPermissions(long guildId, @NotNull String group, @NotNull Set<Permission> permissions) {

    }


    @Override
    public void unsetGroupPermission(long guildId, @NotNull String group, @NotNull String permission) {

    }

    @Override
    public void unsetGroupPermission(long guildId, @NotNull String group, @NotNull Permission permission) {

    }


    @Override
    public void unsetGroupPermissions(long guildId, @NotNull String group, @NotNull Set<Permission> permissions) {

    }


    @Override
    public void createGroup(long guildId, @NotNull String group) {



    }

    @Override
    public void removeGroup(long guildId, @NotNull String group) {

    }


    @Override
    public void reload(@NotNull IModule module) {
        Objects.requireNonNull(module, "module == null");
        reload0(module);
    }

    public void reload0(@Nullable IModule module) {

        if (module != null) {
            sbds.getModuleManager().checkModuleEnabled(module, "Disabled module tried to reload PermissionManager");
            log.info("Module {} requested a reload. Reloading PermissionManager!", module);
        }

        try {

            log.info("Purging cache...");

            cachedGroups.clear();
            cachedUsers.clear();

            userPermissionRepository.reload();
            groupPermissionRepository.reload();
            groupRepository.reload();

        }

        catch (Throwable t) {
            log.error("Failed to reload PermissionManager! Permission checks may work incorrectly!", t);
            return;
        }

        log.info("PermissionManager reloaded successfully!");

    }


    private @NotNull PermissionUser createPermissionUser(long guildId, long userId) {

        int hash = Objects.hash(guildId, userId);

        return cachedUsers.computeIfAbsent(hash, k -> {

            Set<Permission> userPermissions = resolveUserPermissions(guildId, userId);

            PermissionUser out = new PermissionUser(guildId, userId);
            userPermissions.forEach(out::addUserPermission);

            Set<Group> groups = resolveGuildGroups(guildId);
            groups.removeIf(g -> userPermissions.stream().noneMatch(p -> p.permission().equals("group." + g.name())));

            out.addGroups(groups);

            out.rebuildPermissionCache();

            return out;

        });

    }

    private @NotNull Set<Permission> resolveUserPermissions(long guildId, long userId) {
        return userPermissionRepository.getUserPermissions(guildId, userId);
    }

    private @NotNull Set<Group> resolveGuildGroups(long guildId) {

        Set<Group> groupSet = cachedGroups.values().stream().filter(g -> g.guildId() == guildId).collect(Collectors.toSet());
        if (!groupSet.isEmpty()) return groupSet;

        List<GroupData> groupData = groupRepository.getGuildGroups(guildId);
        groupData.forEach(gd -> {

            String name = gd.name();

            Set<Permission> permissions = groupPermissionRepository.getGroupPermissions(guildId, name);
            Group group = new Group(name, gd.id(), guildId, permissions);

            groupSet.add(group);

        });

        groupSet.forEach(g -> cachedGroups.put(g.hashCode(), g));

        return groupSet;

    }


}
