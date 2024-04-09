package cs.youtrade.cstradeapp.util;

import cs.youtrade.cstradeapp.InsertAccController;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesHandler {
    private static PropertiesHandler instance;
    private final Properties properties;

    private PropertiesHandler () {
        this.properties = new Properties();
        try (FileInputStream fis = new FileInputStream("./resources/application.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PropertiesHandler getInstance() {
        if (instance == null) {
            instance = new PropertiesHandler();
        }
        return instance;
    }

    public String readProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }
}
