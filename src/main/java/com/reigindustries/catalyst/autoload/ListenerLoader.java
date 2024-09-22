package com.reigindustries.catalyst.autoload;

import com.reigindustries.catalyst.Catalyst;
import com.reigindustries.catalyst.command.factory.ComplexCommand;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;

public class ListenerLoader {

    private final Plugin plugin = Catalyst.getPlugin();



    public void registerListeners(String... listenerPackageNames) {
        for (String packageName : listenerPackageNames) {
            try {
                for (Class<?> clazz : getClasses(packageName)) {
                    if (Listener.class.isAssignableFrom(clazz)) {
                        registerListener(clazz);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void registerListener(Class<?> listenerClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Listener listenerInstance = (Listener) listenerClass.getDeclaredConstructor().newInstance();
        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        pluginManager.registerEvents(listenerInstance, plugin);
    }


    private Class<?>[] getClasses(String packageName) {
        Reflections reflections = new Reflections(packageName);
        return reflections.getSubTypesOf(Listener.class).toArray(new Class<?>[0]);
    }
}
