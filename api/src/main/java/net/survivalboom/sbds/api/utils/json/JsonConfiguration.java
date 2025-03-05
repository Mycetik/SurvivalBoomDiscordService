package net.survivalboom.sbds.api.utils.json;

import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.bspfsystems.yamlconfiguration.configuration.MemorySection;
import org.bspfsystems.yamlconfiguration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JsonConfiguration extends FileConfiguration {

    @Override
    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {

        try {
            JSONObject json = new JSONObject(contents);
            loadFromJson(json);
        }

        catch (JSONException e) {
            throw new InvalidConfigurationException(e);
        }

    }

    public void loadFromJson(@NotNull JSONObject json) {

        Objects.requireNonNull(json, "json == null");

        this.clear();

        for (String key : json.keySet()) {
            Object value = json.get(key);
            processEntry(key, value);
        }

    }

    /**
     * Рекурсивно обрабатывает входящие пары ключ-значение.
     * Если значение является JSONObject – строим комбинированный ключ с объединением через точку.
     * Если JSONArray – конвертируем в List, иначе вставляем как скаляр.
     */
    private void processEntry(String key, Object value) {

        if (value instanceof JSONObject obj) {
            for (String subKey : obj.keySet()) {
                // Объединяем ключи через точку: родительский ключ + "." + подчинённый
                processEntry(key + "." + subKey, obj.get(subKey));
            }
        }

        else if (value instanceof JSONArray) {
            List<Object> list = convertJsonArrayToList((JSONArray) value);
            insertKey(key.split("\\."), list);
        }

        else {
            insertKey(key.split("\\."), value);
        }

    }

    /**
     * Рекурсивно конвертирует JSONArray в List<Object>.
     * Если встречается JSONObject – конвертирует его в Map через toMap(), если JSONArray – обрабатывает рекурсивно.
     */
    private List<Object> convertJsonArrayToList(JSONArray jsonArray) {

        List<Object> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object item = jsonArray.get(i);
            if (item instanceof JSONObject) {
                // Преобразуем JSONObject в Map
                list.add(((JSONObject) item).toMap());
            }

            else if (item instanceof JSONArray) {
                list.add(convertJsonArrayToList((JSONArray) item));
            }

            else {
                list.add(item);
            }
        }

        return list;
    }

    /**
     * Вставляет значение в конфигурацию, разбивая переданный ключ на сегменты по точке.
     * Если на промежуточном уровне уже находится скаляр, он перемещается под ключ с префиксом "!".
     * Таким образом, для ключа вида "jackson.version.module" получится структура:
     * <p>
     * "jackson": {
     *   "version": {
     *     "!module": "исходное скалярное значение",
     *     "module": { ... вложенные данные ... }
     *   }
     * }
     */
    private void insertKey(String[] segments, Object value) {

        // Начинаем с корневой секции (this)
        MemorySection currentSection = this;
        // Проходим по всем сегментам, кроме последнего (листового)
        for (int i = 0; i < segments.length - 1; i++) {

            String seg = segments[i];
            Object existing = currentSection.get(seg);

            if (existing == null) {
                // Если секция отсутствует, создаем её
                currentSection.createSection(seg);
            }

            else if (!(existing instanceof MemorySection)) {
                // Если по данному ключу уже лежит скаляр – сохраняем его под "!seg"
                currentSection.set("!" + seg, existing);
                // Очищаем место для создания секции
                currentSection.set(seg, null);
                currentSection.createSection(seg);
            }

            // Переходим в следующую вложенную секцию
            currentSection = (MemorySection) currentSection.getConfigurationSection(seg);

        }

        // Обрабатываем последний сегмент – листовой ключ
        String leaf = segments[segments.length - 1];
        Object existingLeaf = currentSection.get(leaf);
        if (existingLeaf != null && !(existingLeaf instanceof MemorySection)) {
            // Если уже есть скалярное значение – перемещаем его под "!leaf"
            currentSection.set("!" + leaf, existingLeaf);
        }

        // Устанавливаем новое значение для листового ключа
        currentSection.set(leaf, value);

    }

    @Override
    public String saveToString() {
        JSONObject json = new JSONObject();
        convertConfigToJson(this, json, "");
        return json.toString(4); // Форматированный вывод с отступами
    }

    /**
     * Рекурсивно обходит конфигурацию и строит JSONObject.
     * Для вложенных секций вызывается convertConfigToJson, для List – конвертация в JSONArray.
     */
    private void convertConfigToJson(FileConfiguration config, JSONObject json, String path) {

        MemorySection section = (MemorySection) config.getConfigurationSection(path);

        if (section == null) return;
        for (String key : section.getKeys(false)) {
            String newPath = path.isEmpty() ? key : path + "." + key;
            Object value = config.get(newPath);
            if (value instanceof MemorySection) {
                JSONObject subJson = new JSONObject();
                convertConfigToJson(config, subJson, newPath);
                json.put(key, subJson);
            } else if (value instanceof List) {
                json.put(key, convertListToJsonArray((List<?>) value));
            } else {
                json.put(key, value);
            }
        }
    }

    /**
     * Конвертирует List в JSONArray.
     * Если элемент списка – Map, то он преобразуется в JSONObject.
     */
    private JSONArray convertListToJsonArray(List<?> list) {
        JSONArray jsonArray = new JSONArray();
        for (Object item : list) {
            if (item instanceof java.util.Map) {
                jsonArray.put(new JSONObject((java.util.Map<?, ?>) item));
            } else if (item instanceof List) {
                jsonArray.put(convertListToJsonArray((List<?>) item));
            } else {
                jsonArray.put(item);
            }
        }
        return jsonArray;
    }
}
