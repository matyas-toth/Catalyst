package com.reigindustries.catalyst.event;

import com.reigindustries.catalyst.Catalyst;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CatalystListener<T extends Event> implements Listener {

    private final Class<T> eventClass;
    private final List<Consumer<T>> eventHandlers;
    private final List<Predicate<T>> cancelConditions;
    private final EventPriority eventPriority;

    public CatalystListener(Class<T> eventClass) {
        this.eventClass = eventClass;
        this.eventHandlers = new ArrayList<>();
        this.cancelConditions = new ArrayList<>();
        this.eventPriority = EventPriority.NORMAL;
        registerEvent();
    }

    public CatalystListener(Class<T> eventClass, EventPriority eventPriority) {
        this.eventClass = eventClass;
        this.eventHandlers = new ArrayList<>();
        this.cancelConditions = new ArrayList<>();
        this.eventPriority = eventPriority;
        registerEvent();
    }


    public CatalystListener<T> perform(Consumer<T> handler) {
        this.eventHandlers.add(handler);
        return this;
    }


    public CatalystListener<T> cancelIf(Predicate<T> condition) {
        this.cancelConditions.add(condition);
        return this;
    }


    private void registerEvent() {
        Catalyst.getPlugin().getServer().getPluginManager().registerEvent(
                this.eventClass,
                this,
                eventPriority,
                (listener, event) -> {
                    if (eventClass.isInstance(event)) {
                        T castedEvent = eventClass.cast(event);


                        boolean shouldCancel = cancelConditions.stream()
                                .anyMatch(condition -> castedEvent instanceof Cancellable && condition.test(castedEvent));

                        if (shouldCancel) {
                            ((Cancellable) castedEvent).setCancelled(true);
                        }


                        if (!(castedEvent instanceof Cancellable) || !((Cancellable) castedEvent).isCancelled()) {
                            for (Consumer<T> handler : eventHandlers) {
                                handler.accept(castedEvent);
                            }
                        }
                    }
                },
                Catalyst.getPlugin()
        );
    }
}
