package com.reigindustries.catalyst.event;

import com.reigindustries.catalyst.Catalyst;
import com.reigindustries.catalyst.autoload.ListenerLoader;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.function.Consumer;

public class Events implements Listener {

    public <T extends Event> CatalystListener<T> on(Class<T> eventClass) {
        return new CatalystListener<T>(eventClass);
    }

    public <T extends Event> CatalystListener<T> on(Class<T> eventClass, EventPriority eventPriority) {
        return new CatalystListener<T>(eventClass, eventPriority);
    }

    @Deprecated
    public <T extends Event> void on(Class<T> eventClass, Consumer<T> handler) {
        Catalyst.getPlugin().getServer().getPluginManager().registerEvent(
                eventClass,
                this,
                org.bukkit.event.EventPriority.NORMAL,
                (listener, event) -> {
                    if (eventClass.isInstance(event)) {
                        handler.accept(eventClass.cast(event));
                    }
                },
                Catalyst.getPlugin()
        );
    }

    @Deprecated
    public <T extends Event> void on(Class<T> eventClass, EventPriority priority, Consumer<T> handler) {
        Catalyst.getPlugin().getServer().getPluginManager().registerEvent(
                eventClass,
                this,
                priority,
                (listener, event) -> {
                    if (eventClass.isInstance(event)) {
                        handler.accept(eventClass.cast(event));
                    }
                },
                Catalyst.getPlugin()
        );
    }

    public void load(String... listenerPackageNames) {
        ListenerLoader ll = new ListenerLoader();
        ll.registerListeners(listenerPackageNames);
    }

}
