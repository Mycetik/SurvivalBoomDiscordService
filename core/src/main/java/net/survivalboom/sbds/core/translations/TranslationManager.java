package net.survivalboom.sbds.core.translations;

import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.translations.ITranslationManager;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.core.SBDS;
import net.survivalboom.sbds.api.utils.Manager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class TranslationManager extends Manager implements ITranslationManager {

    private static final Logger log = LoggerFactory.getLogger("TranslationManager");


    private final SBDS sbds;


    private final File dir;

    private final Map<String, Translation> translationMap = new HashMap<>();


    private Translation defaultTranslation = null;

    private Translation fallbackTranslation = null;


    public TranslationManager(@NotNull SBDS sbds) {
        this.sbds = sbds;
        this.dir = new File(sbds.getWorkingDir(), "translations");
    }


    @Override
    protected void init0() {

        // Створюємо директорію translations якщо її не існує.
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored <- fuck yourself.
            dir.mkdirs();

            Map<String, String> files = Map.of(
                    "translations/translation_en.yml", "translation_en.yml",
                    "translations/translation_ru.yml", "translation_ru.yml",
                    "translations/translation_uk.yml", "translation_uk.yml"
            );
            CommonUtils.checkFiles(TranslationManager.class, dir, files, null);

        }

        // Підвантажуємо усі файли перекладів.
        reload();

        if (!translationMap.isEmpty()) {
            log.info("Successfully loaded {} translations: \n- {}", translationMap.size(), String.join(", ", translationMap.keySet()));
        }

        // Завантажуємо стандартний та резервний переклади.

        String defaultTranslationName = sbds.getConfiguration().getString("translations.default", "null");
        defaultTranslation = getTranslation0(defaultTranslationName);
        if (defaultTranslation == null) log.warn("Default translation with name `{}` not found.", defaultTranslationName);

        String fallbackTranslationName = sbds.getConfiguration().getString("translations.fallback", "null");
        if (fallbackTranslation == null) log.warn("Fallback translation with name `{}` not found.", fallbackTranslationName);

    }

    @Override
    protected void shutdown0() {
        getTranslations0().forEach(Translation::invalid);
        translationMap.clear();
    }


    public synchronized void reload() {

        getTranslations0().forEach(Translation::invalid);
        translationMap.clear();

        for (File file : Objects.requireNonNull(dir.listFiles())) {

            Translation translation;

            try {
                translation = new Translation(file);
            }

            catch (Throwable t) {
                log.error("Failed to load translation `{}`.", file.getName(), t);
                continue;
            }

            translationMap.put(translation.getName(), translation);

        }

    }


    //
    // GETTERS
    //

    @Override
    public @Nullable ITranslation getTranslation(@NotNull String name) {
        return getTranslation0(name);
    }

    public @Nullable Translation getTranslation0(@NotNull String name) {
        checkValid();
        return translationMap.get(name);
    }


    public @NotNull List<Translation> getTranslations0() {
        return new ArrayList<>(translationMap.values());
    }


    public @Nullable Translation defaultTranslation() {
        return defaultTranslation;
    }

    public void defaultTranslation(@Nullable Translation translation) {
        this.defaultTranslation = translation;
    }

    public @Nullable Translation fallbackTranslation() {
        return fallbackTranslation;
    }

    public void fallbackTranslation(@Nullable Translation translation) {
        this.fallbackTranslation = translation;
    }



    public static @NotNull Translation convert(@NotNull ITranslation translation) {
        return (Translation) translation;
    }


}
