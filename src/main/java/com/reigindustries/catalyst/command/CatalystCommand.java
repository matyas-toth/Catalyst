package com.reigindustries.catalyst.command;

import com.reigindustries.catalyst.Catalyst;
import com.reigindustries.catalyst.utils.NMS;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.craftbukkit.v1_19_R2.command.CraftCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CatalystCommand {

    private final String commandName;
    private final List<Consumer<CoreCommand.CommandContext>> commandHandlers;
    private final List<Function<CoreCommand.CommandContext, List<String>>> tabCompleteHandlers;

    public CatalystCommand(String commandName) {
        this.commandName = commandName;
        this.commandHandlers = new ArrayList<>();
        this.tabCompleteHandlers = new ArrayList<>();

        register(Catalyst.getPlugin());
    }


    public CatalystCommand perform(Consumer<CoreCommand.CommandContext> handler) {
        this.commandHandlers.add(handler);
        return this;
    }

    public CatalystCommand autoComplete(Function<CoreCommand.CommandContext, List<String>> tabCompleter) {
        this.tabCompleteHandlers.add(tabCompleter);
        return this;
    }


    public void register(Plugin plugin) {
        CoreCommand command = new CoreCommand(commandName, context -> {
            // Handle command execution
            for (Consumer<CoreCommand.CommandContext> handler : commandHandlers) {
                handler.accept(context);
            }
        });

        // Register the tab completer
        command.setTabCompleter(context -> {
            List<String> completions = new ArrayList<>();
            for (Function<CoreCommand.CommandContext, List<String>> tabCompleter : tabCompleteHandlers) {
                completions.addAll(tabCompleter.apply(context)); // Use the correct CommandContext type
            }
            return completions;
        });

        try {
            String version = NMS.getBukkitVersion();
            Class<?> craftServerClass = Class.forName("org.bukkit.craftbukkit." + version + ".CraftServer");
            Method getCommandMapMethod = craftServerClass.getMethod("getCommandMap");
            Object commandMap = getCommandMapMethod.invoke(plugin.getServer());

            Class<?> simpleCommandMapClass = Class.forName("org.bukkit.craftbukkit." + version + ".command.CraftCommandMap");
            Method registerMethod = simpleCommandMapClass.getMethod("register", String.class, Command.class);

            registerMethod.invoke(commandMap, plugin.getName(), command);


        } catch (Exception e) {
            plugin.getLogger().severe("Error while registering Catalyst command: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

