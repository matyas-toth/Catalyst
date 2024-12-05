package com.reigindustries.catalyst.playerdata;

import com.reigindustries.catalyst.Catalyst;

public class Schema {

    public void set(PlayerDataSchema ds) {
        PlayerDataManager.initialize(Catalyst.getPlugin(), ds.getClass());
        Catalyst.getPlugin().getServer().getPluginManager().registerEvents(new PlayerEventListener(ds.getClass()), Catalyst.getPlugin());
    }

}
