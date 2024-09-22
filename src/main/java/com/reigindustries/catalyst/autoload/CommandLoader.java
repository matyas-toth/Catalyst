package com.reigindustries.catalyst.autoload;

import com.reigindustries.catalyst.Catalyst;
import com.reigindustries.catalyst.command.factory.ComplexCommand;
import com.reigindustries.catalyst.command.factory.annotations.Command;
import com.reigindustries.catalyst.command.factory.annotations.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandLoader {

    private final Plugin plugin = Catalyst.getPlugin();
    private final Map<String, Method> commands = new HashMap<>();



    public void registerCommands(String... commandPackageNames) {
        for (String packageName : commandPackageNames) {
            try {
                for (Class<?> clazz : getClasses(packageName)) {
                    if (ComplexCommand.class.isAssignableFrom(clazz)) {
                        registerCommand(clazz);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void registerCommand(Class<?> commandClass) {
        try {
            Constructor<?> constructor = commandClass.getDeclaredConstructor();
            ComplexCommand commandInstance = (ComplexCommand) constructor.newInstance();

            com.reigindustries.catalyst.command.factory.annotations.Command commandAnnotation =
                    commandClass.getAnnotation(com.reigindustries.catalyst.command.factory.annotations.Command.class);
            if (commandAnnotation != null) {
                String[] commandNames = commandAnnotation.value().split("\\|");
                for (String commandName : commandNames) {

                    PluginCommand pluginCommand = createPluginCommand(commandName.trim());
                    if (pluginCommand != null) {
                        pluginCommand.setExecutor(commandInstance);
                        pluginCommand.setTabCompleter(commandInstance);
                        getCommandMap().register(plugin.getName(), pluginCommand);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private PluginCommand createPluginCommand(String name) throws Exception {
        Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        constructor.setAccessible(true);
        return constructor.newInstance(name, plugin);
    }


    private CommandMap getCommandMap() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private Class<?>[] getClasses(String packageName) {
        Reflections reflections = new Reflections(packageName);
        return reflections.getSubTypesOf(ComplexCommand.class).toArray(new Class<?>[0]);
    }
}
