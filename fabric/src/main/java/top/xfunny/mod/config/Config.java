package top.xfunny.mod.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public abstract class Config {
    private static final Logger LOGGER = LogManager.getLogger(Config.class);
    private final Path configFilePath;

    public Config(Path configFilePath) {
        this.configFilePath = configFilePath;
    }

    @SuppressWarnings("deprecation") // ponytail: gson 2.8.5（MC 1.16.5 内置）无静态 parse 方法，构造器在 2.11 才弃用
    public void readConfig() {
        if (Files.exists(configFilePath)) {
            try {
                JsonObject jsonObject = new JsonParser().parse(String.join("", Files.readAllLines(configFilePath))).getAsJsonObject();
                setTempConfigItems(jsonObject);
            } catch (Exception e) {
                LOGGER.error("Failed to read config file: " + configFilePath, e);
            }
        } else {
            writeConfig();
        }
    }

    public void writeConfig() {
        try {
            Files.createDirectories(configFilePath.getParent());
            Files.write(configFilePath, Collections.singleton(new GsonBuilder().setPrettyPrinting().create().toJson(getTempConfigItems())));
        } catch (IOException e) {
            LOGGER.error("Failed to write config file: " + configFilePath, e);
        }
    }

    protected abstract JsonObject getTempConfigItems();

    protected abstract void setTempConfigItems(JsonObject jsonObject);
}