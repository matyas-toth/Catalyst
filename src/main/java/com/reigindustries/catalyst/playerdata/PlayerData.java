package com.reigindustries.catalyst.playerdata;

import org.bukkit.entity.Player;

public class PlayerData {
    public static Object get(Player player, String key) {
        return PlayerDataManager.get(player, key);
    }

    public static void update(Player player, String key, Object value) {
        PlayerDataManager.update(player, key, value);
    }
}
