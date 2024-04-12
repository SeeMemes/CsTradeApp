package cs.youtrade.cstradeapp.storage;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;

public class TmApiRepository {
    private final static Logger log = LoggerFactory.getLogger(TmApiRepository.class);
    private static ObservableMap<String, UserData> keys;
    private static final String PATH = "./data";
    private static final String FILENAME = "./data/api_keys.json";

    public static ObservableMap<String, UserData> loadApiKeys() {
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, UserData>>() {
        }.getType();
        JsonObject json = loadJsonFromFile();
        Map<String, UserData> map = gson.fromJson(json, mapType);
        if (map == null) {
            return FXCollections.observableHashMap();
        } else {
            return FXCollections.observableMap(map);
        }
    }

    public static void saveApiKey(String uName, UserData data) {
        loadOrCreateKeys();
        keys.put(uName, data);
        saveDataToFile();
    }

    public static void removeUser(String uName) {
        keys.remove(uName);
        saveDataToFile();
    }

    private static void loadOrCreateKeys() {
        if (keys == null) {
            keys = loadApiKeys();
        }
        if (keys == null) {
            keys = FXCollections.observableHashMap();
        }
    }

    private static JsonObject loadJsonFromFile() {
        JsonObject jsonObject = new JsonObject();
        File file = new File(FILENAME);
        try {
            if (!file.exists()) {
                new File(PATH).mkdir();
                file.createNewFile();
                saveDataToFile(jsonObject);
            } else {
                JsonReader reader = new JsonReader(new FileReader(file));
                jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            }
            return jsonObject;
        } catch (FileNotFoundException e) {
            log.error("Couldn't find \"" + FILENAME + "\": " + e.getMessage());
        } catch (IOException | JsonSyntaxException e) {
            if (file.delete()) {
                log.error("Save is broken. Deleting because: " + e.getMessage());
            } else {
                log.error("Couldn't delete file. Error message: " + e.getMessage());
            }
        }
        return jsonObject;
    }

    public static void saveDataToFile() {
        try (FileWriter writer = new FileWriter(FILENAME)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(keys, writer);
        } catch (IOException e) {
            log.error("Couldn't save JSON: " + e.getMessage());
        }
    }

    private static void saveDataToFile(JsonObject jsonObject) {
        try (FileWriter writer = new FileWriter(FILENAME)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            log.error("Couldn't save JSON: " + e.getMessage());
        }
    }

    public static ObservableMap<String, UserData> getKeys() {
        return keys;
    }

    public static void setKeys(ObservableMap<String, UserData> keys) {
        TmApiRepository.keys = keys;
    }
}
