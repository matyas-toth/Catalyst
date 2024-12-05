package com.reigindustries.catalyst.playerdata;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEventListener implements Listener {

    private final Class<?> schema;

    public PlayerEventListener(Class<?> schema) {
        this.schema = schema;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerDataManager.loadData(event.getPlayer(), schema);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerDataManager.saveData(event.getPlayer());
    }

}
