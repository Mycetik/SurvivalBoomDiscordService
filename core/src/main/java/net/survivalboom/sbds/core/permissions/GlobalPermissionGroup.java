package net.survivalboom.sbds.core.permissions;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.permissions.IGlobalGroupPermissionsPool;
import net.survivalboom.sbds.api.permissions.IGlobalPermissionGroup;
import net.survivalboom.sbds.api.permissions.IPermissionManager;
import net.survivalboom.sbds.api.permissions.Permission;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.registrations.RegistrationManager;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.Valid;
import net.survivalboom.sbds.core.registration.InternalRegistrationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalPermissionGroup extends Valid implements IGlobalPermissionGroup, RegistrationManager.Callback<IGlobalGroupPermissionsPool> {

    private final PermissionManager manager;

    protected Registration<IGlobalPermissionGroup> registration;


    private int weight = 0;


    private final InternalRegistrationManager<IGlobalGroupPermissionsPool> registry;

    protected final Map<String, Permission> cache = new HashMap<>();


    public GlobalPermissionGroup(@NotNull PermissionManager manager) {
        this.manager = manager;
        this.registry = new InternalRegistrationManager<>(this, getName(), this, manager.getSbds().getRegistrationRegistry());
        registry.init();
    }

    @Override
    public @NotNull IPermissionManager getManager() {
        return manager;
    }

    @Override
    public @NotNull Registration<IGlobalPermissionGroup> getRegistration() {
        return registration;
    }

    // weight //

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public void setWeight(int weight) {
        this.weight = weight;
    }

    //
    // PERMISSIONS
    //

    @Override
    public @Nullable Permission getPermission(@NotNull String permission) {
        return cache.get(permission);
    }

    @Override
    public @NotNull Map<String, Permission> getPermissions() {
        return new HashMap<>(cache);
    }

    //
    // POOLS
    //

    // CREATE //

    @Override
    public @NotNull IGlobalGroupPermissionsPool createPool(@NotNull IModule module, @NotNull String name) {
        Objects.requireNonNull(module, "module == null");
        return createPool0(module, name);
    }

    public @NotNull GlobalGroupPermissionPool createPool0(@Nullable IModule module, @NotNull String name) {

        Objects.requireNonNull(name, "name == null");
        checkValid();

        GlobalGroupPermissionPool pool = new GlobalGroupPermissionPool(this);
        pool.registration = registry.register0(module, name, pool);

        return pool;

    }

    // REMOVE //

    @Override
    public void removePool(@NotNull IGlobalGroupPermissionsPool pool) {

        checkValid();

        if (registry.unregister(pool) != null) {
            throw new IllegalArgumentException("Invalid pool `" + pool + "`");
        }

    }

    @Override
    public void unRegister(@NotNull Registration<IGlobalGroupPermissionsPool> registration) {
        recalculateFullCache();
    }

    // GET //

    @Override
    public @Nullable IGlobalGroupPermissionsPool getPool(@NotNull NamespacedKey key) {
        checkValid();
        return registry.getRegistrationAsObject(key);
    }

    @Override
    public @NotNull List<IGlobalGroupPermissionsPool> getPools() {
        checkValid();
        return new ArrayList<>(registry.getRegisteredObjects());
    }

    //
    // MISC
    //

    private void recalculateFullCache() {

        Map<String, Permission> out = getPools().stream()
                .flatMap(pool -> pool.getPermissions().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.cache.clear();
        this.cache.putAll(out);

    }

}
