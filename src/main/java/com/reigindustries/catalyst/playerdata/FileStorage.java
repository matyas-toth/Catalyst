package com.reigindustries.catalyst.playerdata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

import org.yaml.snakeyaml.Yaml;

public class FileStorage {
    private static final File DATA_FOLDER = new File("catalyst/PlayerData");

    static {
        if (!DATA_FOLDER.exists()) {
            DATA_FOLDER.mkdirs();
        }
    }

    public static Map<String, Object> load(UUID uuid) {
        File file = new File(DATA_FOLDER, uuid.toString() + ".yml");
        if (!file.exists()) return Map.of();

        try {
            Yaml yaml = new Yaml();
            return yaml.load(Files.readString(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    public static void save(UUID uuid, Map<String, Object> data) {
        File file = new File(DATA_FOLDER, uuid.toString() + ".yml");

        try {
            Yaml yaml = new Yaml();
            Files.writeString(file.toPath(), yaml.dump(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
