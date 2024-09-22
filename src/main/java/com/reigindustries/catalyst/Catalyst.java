package com.reigindustries.catalyst;

import com.reigindustries.catalyst.command.Commands;
import com.reigindustries.catalyst.event.Events;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public final class Catalyst {

    private static Plugin plugin;
    private static Events events = new Events();
    private static Commands commands = new Commands();

    public static void init(Plugin plugin) {
        Catalyst.plugin = plugin;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static Events events() {
        return events;
    }

    public static Commands commands() {
        return commands;
    }

}
