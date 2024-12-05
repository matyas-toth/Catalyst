package com.reigindustries.catalyst.utils;

import com.reigindustries.catalyst.Catalyst;
import org.bukkit.Bukkit;

public class NMS {

    public static String getBukkitVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

}
