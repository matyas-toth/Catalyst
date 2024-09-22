package com.reigindustries.catalystexample;

import com.reigindustries.catalyst.Catalyst;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CatalystExample extends JavaPlugin {

    @Override
    public void onEnable() {

        Catalyst.init(this);

        Catalyst.commands().load("com.reigindustries.catalystexample.commands");
        Catalyst.events().load("com.reigindustries.catalystexample.listeners");

        Catalyst.events().on(PlayerJoinEvent.class)
                .perform(e -> e.getPlayer().sendMessage("Hello here!"));

        Catalyst.commands().on("testcommand").perform(c -> c.getPlayer().sendMessage("This is a test debug command"));

    }

}
