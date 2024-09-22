package com.reigindustries.catalyst.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinLIstener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        e.getPlayer().sendMessage("Hello");

    }

}
