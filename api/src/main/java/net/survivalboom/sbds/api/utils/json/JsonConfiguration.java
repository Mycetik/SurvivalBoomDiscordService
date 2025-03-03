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
import java.util.Map;
import java.util.Objects;

public class JsonConfiguration extends FileConfiguration {

    public void loadFromJson(@NotNull JSONObject json) {

        Objects.requireNonNull(json, "json == null");

        // Преобразуем JSONObject в Map и загружаем в конфигурацию
        Map<String, Object> map = json.toMap();

        // Этот метод зависит от реализации FileConfiguration,
        // обычно надо очистить внутреннее хранилище и добавить новые данные.
        this.clear();
        convertJsonToConfig(json, this, "");

    }

    private void convertJsonToConfig(JSONObject json, FileConfiguration config, String path) {
        for (String key : json.keySet()) {
            Object value = json.get(key);
            String newPath = path.isEmpty() ? key : path + "." + key;

            if (value instanceof JSONObject) {
                // Рекурсивно создаем секции
                convertJsonToConfig((JSONObject) value, config, newPath);
            } else if (value instanceof JSONArray) {
                // Конвертируем JSONArray в List
                List<Object> list = convertJsonArrayToList((JSONArray) value);
                config.set(newPath, list);
            } else {
                config.set(newPath, value);
            }
        }
    }

    private List<Object> convertJsonArrayToList(JSONArray jsonArray) {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object item = jsonArray.get(i);
            if (item instanceof JSONObject) {
                list.add(((JSONObject) item).toMap()); // Конвертируем JSONObject в Map
            } else {
                list.add(item);
            }
        }
        return list;
    }

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

    @Override
    public String saveToString() {
        JSONObject json = new JSONObject();
        convertConfigToJson(this, json, "");
        return json.toString(4); // 4 — красивый отступ
    }


    private void convertConfigToJson(FileConfiguration config, JSONObject json, String path) {

        for (String key : config.getConfigurationSection(path).getKeys(false)) {
            String newPath = path.isEmpty() ? key : path + "." + key;
            Object value = config.get(newPath);

            if (value instanceof MemorySection) {
                JSONObject subJson = new JSONObject();
                convertConfigToJson(config, subJson, newPath);
                json.put(key, subJson);
            }

            else {
                json.put(key, value);
            }
        }
    }

}
