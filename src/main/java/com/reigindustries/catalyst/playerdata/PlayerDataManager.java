package com.reigindustries.catalyst.playerdata;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataManager {
    private static final Map<Player, Object> playerData = new HashMap<>();
    private static BukkitTask periodicSaveTask;

    public static void initialize(Plugin plugin, Class<?> schema) {
        periodicSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, PlayerDataManager::saveAll, 0, 2400L); // Every 2 minutes
    }

    public static void shutdown() {
        if (periodicSaveTask != null) {
            periodicSaveTask.cancel();
        }
        saveAll();
    }

    public static void loadData(Player player, Class<?> schema) {
        try {
            Object instance = schema.getDeclaredConstructor().newInstance();

            // Load data from file
            Map<String, Object> loadedData = FileStorage.load(player.getUniqueId());

            for (Field field : schema.getDeclaredFields()) {
                if (field.isAnnotationPresent(PlayerField.class)) {
                    field.setAccessible(true);
                    String key = field.getAnnotation(PlayerField.class).key();
                    Object value = loadedData.getOrDefault(key, field.get(instance)); // Use default value if absent
                    field.set(instance, value);
                }
            }

            playerData.put(player, instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveData(Player player) {
        try {
            Object schemaInstance = playerData.get(player);

            if (schemaInstance == null) return;

            Map<String, Object> dataToSave = new HashMap<>();

            for (Field field : schemaInstance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(PlayerField.class)) {
                    field.setAccessible(true);
                    String key = field.getAnnotation(PlayerField.class).key();
                    dataToSave.put(key, field.get(schemaInstance));
                }
            }

            FileStorage.save(player.getUniqueId(), dataToSave);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveAll() {
        Bukkit.getOnlinePlayers().forEach(PlayerDataManager::saveData);
    }

    public static Object get(Player player, String key) {
        Object schemaInstance = playerData.get(player);

        if (schemaInstance == null) return null;

        for (Field field : schemaInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(PlayerField.class)) {
                String fieldKey = field.getAnnotation(PlayerField.class).key();
                if (fieldKey.equals(key)) {
                    field.setAccessible(true);
                    try {
                        return field.get(schemaInstance);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static void update(Player player, String key, Object value) {
        Object schemaInstance = playerData.get(player);

        if (schemaInstance == null) return;

        for (Field field : schemaInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(PlayerField.class)) {
                String fieldKey = field.getAnnotation(PlayerField.class).key();
                if (fieldKey.equals(key)) {
                    field.setAccessible(true);
                    try {
                        field.set(schemaInstance, value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

