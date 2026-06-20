package net.survivalboom.sbds.api.modules;

import net.survivalboom.sbds.api.ISBDS;
import net.survivalboom.sbds.api.libraries.LibraryDeclaration;
import net.survivalboom.sbds.api.modules.dependencies.ModuleDependency;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public interface IModuleManager extends IManager {

    @NotNull ISBDS getSbds();

    @NotNull File getModulesDir();

    //
    // MODULES
    //

    // LOADING & UNLOADING //

    @NotNull ModuleMetaLoadResult loadModuleMeta(@NotNull File file) throws ModuleMeta.InvalidMetaException;

    @NotNull IModule loadModule(@NotNull File file) throws ModuleLoadingException, ModuleRefusedException, ModuleUnsatisfiedDependencyException, ModuleUnsatisfiedLibraryException;

    @NotNull IModule createModule(@NotNull ModuleMeta meta, @Nullable ModuleFile file, @Nullable File dataDir) throws ModuleLoadingException, ModuleRefusedException, ModuleUnsatisfiedDependencyException, ModuleUnsatisfiedLibraryException;

    default @NotNull IModule createModule(@NotNull ModuleMetaLoadResult meta) throws ModuleLoadingException, ModuleUnsatisfiedLibraryException, ModuleUnsatisfiedDependencyException, ModuleRefusedException {
        return createModule(meta.meta, meta.file, null);
    }


    void unloadModule(@NotNull IModule module) throws ModuleStateCallbackException, ModuleDependantException;

    default void unloadModule(@NotNull ModuleMain moduleMain) throws ModuleStateCallbackException, ModuleDependantException {
        unloadModule(moduleMain.getModule());
    }

    // ENABLING & DISABLING //

    void enableModule(@NotNull IModule module) throws ModuleStateCallbackException, ModuleRefusedException;

    default void enableModule(@NotNull ModuleMain moduleMain) throws ModuleStateCallbackException, ModuleRefusedException {
        enableModule(moduleMain.getModule());
    }


    void disableModule(@NotNull IModule module) throws ModuleStateCallbackException;

    default void disableModule(@NotNull ModuleMain moduleMain) throws ModuleStateCallbackException {
        disableModule(moduleMain.getModule());
    }

    // GETTERS //

    @Nullable IModule getModule(@NotNull String id);

    @NotNull List<IModule> getModules();

    // MISC //

    @NotNull IModule checkModuleValid(@NotNull IModule module);

    @NotNull IModule checkModuleEnabled(@NotNull IModule imodule, @Nullable String message);

    //
    // NAMES
    //

    String ALLOWED_NAME_CHARACTERS = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIGKLMNOPQRSTUVWXYZ";

    String ALLOWED_ID_CHARACTERS = "abcdefghijklmnopqrstuvwxyz1234567890";

    //
    // RECORDS
    //

    record ModuleMetaLoadResult(@NotNull ModuleMeta meta, @NotNull ModuleFile file) {}

    //
    // EXCEPTIONS
    //

    class ModuleLoadingException extends Exception {

        public ModuleLoadingException(String message, Throwable cause) {
            super(message, cause);
        }

        public ModuleLoadingException(String message) {
            super(message);
        }

        public ModuleLoadingException(Throwable t) {
            super(t.getMessage(), t);
        }

    }

    class ModuleRefusedException extends Exception {

        public ModuleRefusedException(String message) {
            super(message);
        }

    }

    class ModuleStateCallbackException extends Exception {

        public ModuleStateCallbackException(Throwable e) {
            super(e);
        }

    }

    class ModuleUnsatisfiedLibraryException extends Exception {

        private final LibraryDeclaration library;

        public ModuleUnsatisfiedLibraryException(@NotNull LibraryDeclaration library, @NotNull String message, @NotNull Throwable cause) {
            super(message, cause);
            this.library = library;
        }

        public @NotNull LibraryDeclaration getLibrary() {
            return library;
        }

    }

    class ModuleUnsatisfiedDependencyException extends Exception {

        private final ModuleDependency dependency;

        public ModuleUnsatisfiedDependencyException(@NotNull ModuleDependency dependency, @NotNull String message) {
            super(message);
            this.dependency = dependency;
        }

        public @NotNull ModuleDependency getDependency() {
            return dependency;
        }

    }

    class ModuleDependantException extends Exception {

        private final IModule module;

        public ModuleDependantException(@NotNull IModule module, @NotNull String message) {
            super(message);
            this.module = module;
        }

        public @NotNull IModule getModule() {
            return module;
        }

    }

}
