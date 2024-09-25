package com.reigindustries.catalyst.internal.config;

import org.bukkit.ChatColor;

import java.util.HashMap;

public class Config {

    private static HashMap<Option, String> settings;



    public static void init() {

        // ${command}, ${type}, ${parameter}

        settings.put(Option.COMMAND_NO_PERMISSION_MESSAGE, ChatColor.RED + "You don't have permission to do this!");
        settings.put(Option.COMMAND_INVALID_PARAMETER_MESSAGE, ChatColor.RED + "Incomplete command. Expected ${type} at parameter ${parameter}.");
        settings.put(Option.COMMAND_MISSING_REQUIRED_PARAMETER_MESSAGE, ChatColor.RED + "Missing required parameter at position ${parameter}.");
        settings.put(Option.COMMAND_REQUIRES_PLAYER_MESSAGE, ChatColor.RED + "Only players can run this command!");

    }

    public static void set(Option key, String value) {
        settings.put(key, value);
    }
    public static String get(Option key) { return settings.get(key); }

}
