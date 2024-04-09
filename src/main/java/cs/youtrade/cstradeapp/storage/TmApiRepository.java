package cs.youtrade.cstradeapp.storage;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TmApiRepository {
    private final static Logger log = LoggerFactory.getLogger(TmApiRepository.class);
    private static final String PATH = "./data";
    private static final String FILENAME = "./data/api_keys.json";

    public static List<UserData> loadApiKeys() {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<UserData>>(){}.getType();
        JsonArray json = loadJsonFromFile();
        return gson.fromJson(json, listType);
    }

    public static void saveApiKey(UserData data) {
        List<UserData> dataList = loadApiKeys();
        dataList.add(data);
        saveDataToFile(dataList);
    }

    private static JsonArray loadJsonFromFile() {
        JsonArray jsonArray = new JsonArray();
        File file = new File(FILENAME);
        try {
            if (!file.exists()) {
                new File(PATH).mkdir();
                file.createNewFile();
            } else {
                JsonReader reader = new JsonReader(new FileReader(file));
                jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            }
        } catch (FileNotFoundException e) {
            log.error("Couldn't find \"" + FILENAME + "\": " + e.getMessage());
        } catch (IOException | JsonSyntaxException e) {
            if (file.delete()) {
                log.error("Save is broken. Deleting because: " + e.getMessage());
            }
            else {
                log.error("Couldn't delete file. Error message: " + e.getMessage());
            }
        }
        return jsonArray;
    }

    public static void saveDataToFile(List<UserData> apiKeyMap) {
        try (FileWriter writer = new FileWriter(FILENAME)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(apiKeyMap, writer);
        } catch (IOException e) {
            log.error("Couldn't save JSON: " + e.getMessage());
        }
    }

    public static void saveDataToFile(JsonObject jsonObject) {
        try (FileWriter writer = new FileWriter(FILENAME)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            log.error("Couldn't save JSON: " + e.getMessage());
        }
    }
}
