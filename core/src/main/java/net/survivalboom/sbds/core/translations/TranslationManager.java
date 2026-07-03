package net.survivalboom.sbds.core.translations;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.translations.ITranslationManager;
import net.survivalboom.sbds.api.translations.ITranslationsMessagesPool;
import net.survivalboom.sbds.api.translations.InvalidTranslationException;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.core.registration.InternalRegistrationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TranslationManager extends Manager implements ITranslationManager {

    private static final Logger log = LoggerFactory.getLogger("TranslationManager");


    protected final SBDS sbds;


    private final File sbdsTranslationsDir;

    private final InternalRegistrationManager<ITranslation> registry;


    private @Nullable ITranslation defaultTranslation = null;
    private @Nullable ITranslation fallbackTranslation = null;


    public TranslationManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.registry = new InternalRegistrationManager<>(this, "translation", null, sbds.getRegistrationRegistry());
        this.sbdsTranslationsDir = new File(sbds.getWorkingDir(), "translations");
    }

    //
    // MANAGER
    //

    @Override
    protected void init0() {

        // Підвантажуємо усі файли перекладів.
        reload0();

        var translations = registry.getRegistrations();
        if (!translations.isEmpty()) {
            String strings = String.join(", ", translations.stream()
                    .map(Registration::key)
                    .map(NamespacedKey::toString)
                    .toList()
            );
            log.info("Successfully loaded {} translations: \n- {}", translations.size(), strings);
        }

    }

    @Override
    protected void shutdown0() {
        registry.shutdown();
    }

    @Override
    public void reload(@NotNull IModule module) {
        sbds.getModuleManager().checkModuleEnabled(module, "Disabled module attempted to reload TranslationManager");
        log.warn("A reload was requested from `{}`. Reloading...", module.getName());
        reload();
    }

    public void reload() {

        try {
            reload0();
        }

        catch (Exception e) {
            log.error("Failed to reload properly. An unknown exception occurred.", e);
        }

    }

    private synchronized void reload0() {

        // Перезапускаємо реєстр аби очистити його від усіх перекладів.

        List<IModule> modulesToRegister;
        if (registry.isValid()) {

            // Якщо перезавантаження відбувається вже після реєстрації повідомлень модулями, ми отримаємо відвал усіх повідомлень модулів.
            // Тож спочатку запам'ятаємо які модулі реєстрували свої повідомлення, щоб потім додати їх назад.

            var regs = registry.getRegistrations();

            modulesToRegister = regs.stream()
                    .flatMap(reg -> reg.object().getMessagePools().stream())
                    .map(ITranslationsMessagesPool::getRegistration)
                    .map(Registration::module)
                    .filter(Objects::nonNull)
                    .toList();

            registry.shutdown();

        } else {
            modulesToRegister = null;
        }

        registry.init();

        // Якщо директорія translations не існує, певно ми запускаємось вперше або користувач вирішив усе наїбнути.
        // Не дамо користувачу поламати нашого шикарного бота!
        // Створюємо директорію та закидуємо у неї базові переклади SBDS.
        if (!sbdsTranslationsDir.exists()) {

            //noinspection ResultOfMethodCallIgnored <- fuck yourself.
            sbdsTranslationsDir.mkdirs();

            Map<String, String> files = Map.of(
                    "translations/translation_en.yml", "translation_en.yml",
                    "translations/translation_ru.yml", "translation_ru.yml",
                    "translations/translation_uk.yml", "translation_uk.yml"
            );
            CommonUtils.checkFiles(TranslationManager.class, sbdsTranslationsDir, files, null);

        }

        // Підвантажуємо переклади.

        //noinspection DataFlowIssue <- іді нахуй сука блять
        for (File file : sbdsTranslationsDir.listFiles()) {

            if (!file.getName().endsWith(".yml")) {
                continue;
            }

            try {
                loadTranslation0(null, file);
            }

            catch (Exception e) {
                log.error("Failed to load translation file `{}`. Skipping...", file.getName(), e);
            }

        }

        // Завантажуємо стандартний та резервний переклади.

        String defaultTranslationName = sbds.getConfiguration().node("translations", "default").getString("null");
        defaultTranslation = findTranslationByInvalidName0(defaultTranslationName);
        if (defaultTranslation == null) {
            log.warn("Default translation with name `{}` not found.", defaultTranslationName);
        }

        String fallbackTranslationName = sbds.getConfiguration().node("translations", "fallback").getString("null");
        fallbackTranslation = findTranslationByInvalidName0(fallbackTranslationName);
        if (fallbackTranslation == null) {
            log.warn("Fallback translation with name `{}` not found.", fallbackTranslationName);
        }

        // Перереєстровуємо повідомлення модулів.
        if (modulesToRegister != null) {

            for (IModule module : modulesToRegister) {
                importModuleMessages(module);
            }

        }

    }

    private @Nullable ITranslation findTranslationByInvalidName0(String str) {

        try {
            return getTranslation(str);
        }

        catch (Exception e) {
            return null;
        }

    }

    //
    // TRANSLATIONS MANAGEMENT
    //

    // REGISTRATION //

    @Override
    public @NotNull ITranslation createTranslation(@NotNull IModule module, @NotNull String name) {
        Objects.requireNonNull(module, "module == null");
        return createTranslation0(module, name);
    }

    public @NotNull Translation createTranslation0(@Nullable IModule module, @NotNull String name) {

        Objects.requireNonNull(name, "name == null");
        checkValid();

        if (module != null) {
            sbds.getModuleManager().checkModuleEnabled(module, "Disabled module attempted to create a translation");
        }

        Translation translation = new Translation(name, this);
        translation.registration = registry.register0(module, name, translation);

        return translation;

    }

    // LOADING //

    @Override
    public @NotNull ITranslation loadTranslation(@NotNull IModule module, @NotNull ConfigurationNode section) throws InvalidTranslationException {
        Objects.requireNonNull(module, "module == null");
        return loadTranslation0(module, section);
    }

    public @NotNull ITranslation loadTranslation0(@Nullable IModule module, @NotNull ConfigurationNode section) throws InvalidTranslationException {

        Objects.requireNonNull(section, "section == null");
        checkValid();

        String name = section.node("$name").getString();
        if (name == null) {
            throw new InvalidTranslationException("Invalid translation. Key `$name` not found");
        }

        NamespacedKey key;
        try {
            key = module != null ? NamespacedKey.fromModule(module, name) : NamespacedKey.sbds(name);
        }

        catch (IllegalArgumentException e) {
            throw new InvalidTranslationException("Invalid translation name `" + name + "`. " + e.getMessage());
        }

        if (getTranslation(key) != null) {
            throw new IllegalStateException("Translation with name `" + name + "` already exists");
        }

        Translation translation = createTranslation0(null, name);

        String displayName = section.node("$displayName").getString();
        String translationEmoji = section.node("$icon").getString();
        DiscordLocale locale;
        try {
            locale = section.node("$locale").get(DiscordLocale.class);
        } catch (SerializationException e) {
            locale = null;
        }

        translation.setDisplayName(displayName);
        translation.setIconEmoji(translationEmoji);
        translation.setDiscordLocale(locale);

        // Завантажуємо повідомлення перекладу //

        var result = translation.createMessagesPool0(null, "messages").load(section, true);
        for (var entry : result.failed().entrySet()) {
            log.error("[{}] Failed to load message `{}`. Skipping...", key, entry.getKey(), entry.getValue());
        }

        return translation;

    }

    @Override
    public @NotNull ITranslation loadTranslation(@NotNull IModule module, @NotNull File file) throws ConfigurateException, InvalidTranslationException {
        Objects.requireNonNull(module, "module == null");
        return loadTranslation0(module, file);
    }

    public @NotNull ITranslation loadTranslation0(@Nullable IModule module, @NotNull File file) throws ConfigurateException, InvalidTranslationException {

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(file.toPath())
                .build();

        ConfigurationNode node = loader.load();

        return loadTranslation0(module, node);

    }


    // REMOVING //

    @Override
    public boolean removeTranslation(@NotNull ITranslation translation) {
        checkValid();
        return registry.unregister(translation.getRegistration());
    }


    //
    // GETTERS
    //

    @Override
    public @Nullable ITranslation getTranslation(@NotNull NamespacedKey key) {
        return registry.getRegistrationAsObject(key);
    }

    @Override
    public @NotNull List<ITranslation> getTranslations() {
        checkValid();
        return registry.getRegisteredObjects();
    }

    //
    // FALLBACK
    //

    // default translation //

    @Override
    public @Nullable ITranslation getDefaultTranslation() {
        return defaultTranslation;
    }

    @Override
    public void setDefaultTranslation(@Nullable ITranslation translation) {

        checkValid();

        if (translation != null && registry.getObjectRegistration(translation) == null) {
            throw new IllegalArgumentException("Unknown translation object; Are you trying to break something?");
        }

        this.defaultTranslation = translation;

    }

    // fallback translation //

    @Override
    public @Nullable ITranslation getFallbackTranslation() {
        return fallbackTranslation;
    }

    @Override
    public void setFallbackTranslation(@Nullable ITranslation translation) {

        checkValid();

        if (translation != null && registry.getObjectRegistration(translation) == null) {
            throw new IllegalArgumentException("Unknown translation object; Are you trying to break something?");
        }

        this.fallbackTranslation = translation;

    }

    //
    // MODULES MESSAGES
    //

    @Override
    public void importModuleMessages(@NotNull IModule module, @NotNull String dirName) {

        checkValid();
        sbds.getModuleManager().checkModuleEnabled(module, "Disabled module attempted to import messages");

        File dir = new File(module.getDataFolder(), dirName);
        if (!dir.exists()) {
            log.warn("Failed to import messages from module `{}`. Directory `{}` does not exist in module directory.", module.getName(), dirName);
            return;
        }

        //noinspection DataFlowIssue <- дінаху
        for (File file : dir.listFiles()) {

            if (!file.getName().endsWith(".yml")) {
                continue;
            }

            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(file.toPath())
                    .build();

            ConfigurationNode section;
            try {
                section = loader.load();
            }

            catch (ConfigurateException e) {
                log.error("Failed to load file `{}`. An exception occurred.", file.getName(), e);
                continue;
            }

            String name = section.node("$name").getString();
            if (name == null) {
                log.warn("Messages file `{}` does not contain `$name` key.", file.getName());
                continue;
            }

            NamespacedKey key;
            try {
                key = NamespacedKey.fromString(name);
            }

            catch (IllegalArgumentException e) {
                log.warn("Messages file `{}` has invalid translation key `{}`. Skipping...", file.getName(), name, e);
                continue;
            }

            ITranslation translation = getTranslation(key);
            if (translation == null) {
                continue;
            }

            ITranslationsMessagesPool pool = translation.obtainMessagesPool(module, dirName);
            var result = pool.load(section, true);

            for (var entry : result.failed().entrySet()) {
                log.error("[{}] Failed to load message `{}`. Skipping...", key, entry.getKey(), entry.getValue());
            }

        }

    }

    //
    // DISCORD LOCALE
    //

    @Override
    public @Nullable ITranslation findTranslationByLocale(@NotNull DiscordLocale locale) {

        Objects.requireNonNull(locale, "locale == null");
        checkValid();

        return getTranslations().stream()
                .filter(t -> locale.equals(t.getDiscordLocale()))
                .findAny()
                .orElse(null);

    }

    @Override
    public @NotNull Set<DiscordLocale> getAvailableLocales() {
        checkValid();
        return getTranslations().stream()
                .map(ITranslation::getDiscordLocale)
                .collect(Collectors.toSet());
    }

}
