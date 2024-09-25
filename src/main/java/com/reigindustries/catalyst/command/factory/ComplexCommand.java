package com.reigindustries.catalyst.command.factory;

import com.reigindustries.catalyst.Catalyst;
import com.reigindustries.catalyst.command.CatalystCommand;
import com.reigindustries.catalyst.command.factory.annotations.*;
import com.reigindustries.catalyst.command.factory.annotations.Optional;
import com.reigindustries.catalyst.internal.config.Config;
import com.reigindustries.catalyst.internal.config.Option;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public abstract class ComplexCommand implements CommandExecutor, TabCompleter {

    private CommandSender sender;
    private Player player;
    private List<String> args = new ArrayList<>();
    private final Map<String, Method> subCommands = new HashMap<>();
    private Method indexMethod = null;
    protected String expectedArgumentType;
    protected Integer expectedArgumentIndex;




    public ComplexCommand() {
        // Register subcommands
        for (Method method : this.getClass().getMethods()) {
            if (method.isAnnotationPresent(Subcommand.class)) {
                Subcommand subcommand = method.getAnnotation(Subcommand.class);
                for (String alias : subcommand.value().split("\\|")) {
                    subCommands.put(alias, method);
                }
            } else if (method.isAnnotationPresent(Index.class)) {
                indexMethod = method;
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.sender = sender;
        this.args = List.of(args);

        if (sender instanceof Player) {
            this.player = (Player) sender;
        }

        if (sender instanceof Player) {
            if (!sender.hasPermission(getClass().getAnnotation(Permission.class).value())) {
                noPermission();
                return true;
            }
        } else {

            if(getClass().isAnnotationPresent(RequiresPlayer.class)) {
                requiresPlayer();
                return true;
            }

        }



        if(subCommands.isEmpty()) {

            if (indexMethod == null) {
                sender.sendMessage("§4§lCatalyst Error: §r§cNo @Index method found. Please report this to a system administrator.");
                return true;
            }

            Class<?>[] paramTypes = indexMethod.getParameterTypes();
            Object[] paramValues = new Object[paramTypes.length];
            Annotation[][] paramAnnotations = indexMethod.getParameterAnnotations();

            for (int i = 0; i < paramTypes.length; i++) {

                boolean isOptional = false;
                boolean isJoin = false;

                for (Annotation annotation : paramAnnotations[i]) {
                    if (annotation.annotationType() == Optional.class) {
                        isOptional = true;

                    } else if(annotation.annotationType() == Join.class) {
                        isJoin = true;

                    }
                }

                if(i + 1 == paramTypes.length && paramTypes[i] == String.class && isJoin) {

                    String concatenated = "";

                    if(i >= args.length) {
                        concatenated = null;
                    } else {
                        for(int y = i; y < args.length; y++) {
                            if(y + 1 != args.length) {
                                concatenated = concatenated + args[y] + " ";
                            } else {
                                concatenated = concatenated + args[y];
                            }
                        }

                    }

                    paramValues[i] = concatenated;



                }

                if (i < args.length) {


                    if (paramTypes[i] == String.class && !isJoin) {
                        paramValues[i] = args[i];
                    } else if (paramTypes[i] == Integer.class) {
                        try {
                            paramValues[i] = Integer.parseInt(args[i]);
                        } catch (NumberFormatException e) {

                            this.expectedArgumentType = "Integer";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == Double.class) {
                        try {
                            paramValues[i] = Double.parseDouble(args[i]);
                        } catch (NumberFormatException e) {
                            this.expectedArgumentType = "Double";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == Float.class) {
                        try {
                            paramValues[i] = Float.parseFloat(args[i]);
                        } catch(NumberFormatException e) {
                            this.expectedArgumentType = "Float";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == Material.class) {
                        if(Material.getMaterial(args[i]) == null) {
                            this.expectedArgumentType = "Material";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        } else {
                            paramValues[i] = Material.getMaterial(args[i]);
                        }
                    } else if (paramTypes[i] == World.class) {
                        if(Bukkit.getWorld(args[i]) == null) {
                            this.expectedArgumentType = "World";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        } else {
                            paramValues[i] = Bukkit.getWorld(args[i]);
                        }
                    } else if (paramTypes[i] == Player.class) {
                        if(Bukkit.getPlayer(args[i]) == null) {
                            this.expectedArgumentType = "Player";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        } else {
                            paramValues[i] = Bukkit.getPlayer(args[i]);
                        }
                    } else if (paramTypes[i] == OfflinePlayer.class) {
                        paramValues[i] = Bukkit.getOfflinePlayer(args[i]);
                    } else if (paramTypes[i] == Boolean.class) {
                        paramValues[i] = Boolean.parseBoolean(args[i]);
                    } else if (paramTypes[i].isEnum()) {
                        try {
                            paramValues[i] = Enum.valueOf((Class<Enum>) paramTypes[i], args[i].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = paramTypes[i].getSimpleName();
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == UUID.class) {
                        try {
                            paramValues[i] = UUID.fromString(args[i]);
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = "UUID";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == PotionEffectType.class) {
                        PotionEffectType effect = PotionEffectType.getByName(args[i].toUpperCase());
                        if (effect == null) {
                            this.expectedArgumentType = "Potion Effect";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                        paramValues[i] = effect;
                    } else if (paramTypes[i] == Enchantment.class) {
                        Enchantment enchantment = Enchantment.getByName(args[i].toUpperCase());
                        if (enchantment == null) {
                            this.expectedArgumentType = "Enchantment";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                        paramValues[i] = enchantment;
                    } else if (paramTypes[i] == Sound.class) {
                        try {
                            paramValues[i] = Sound.valueOf(args[i].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = "Sound";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == ChatColor.class) {
                        try {
                            paramValues[i] = ChatColor.valueOf(args[i].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = "Color";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == Difficulty.class) {
                        try {
                            paramValues[i] = Difficulty.valueOf(args[i].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = "Difficulty";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == GameMode.class) {
                        try {
                            paramValues[i] = GameMode.valueOf(args[i].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = "GameMode";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == Biome.class) {
                        Biome biome = Biome.valueOf(args[i].toUpperCase());
                        if (biome == null) {
                            this.expectedArgumentType = "Biome";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                        paramValues[i] = biome;
                    } else if (paramTypes[i] == WeatherType.class) {
                        try {
                            paramValues[i] = WeatherType.valueOf(args[i].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = "Weather";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == EntityType.class) {
                        try {
                            paramValues[i] = EntityType.valueOf(args[i].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = "Entity Type";
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    }

                    if (paramValues[i] == null) {
                        return true;
                    }
                } else if (isOptional) {

                    paramValues[i] = getDefaultValue(paramTypes[i]);
                } else {

                    this.expectedArgumentIndex = i;
                    missingRequiredArgument();
                    return true;
                }

            }

            try {
                indexMethod.invoke(this, paramValues);
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage("§4§lCatalyst Error: §r§cCould not execute @Index method, see console for more. Please report this to a system administrator.");
            }

            ///

            return true;
        }

        if (args.length == 0) {
            try {
                indexMethod.invoke(this);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage("§4§lCatalyst Error: §r§cCould not execute @Index method, see console for more. Please report this to a system administrator.");
            }
        }

        if (sender instanceof Player) {
            if (!sender.hasPermission(getClass().getAnnotation(Permission.class).value())) {
                noPermission();
                return true;
            }
        } else {
            // Handle non-player command senders if necessary
        }

        String subcommandName = args[0];
        Method subCommand = subCommands.get(subcommandName);

        if (subCommand != null) {

            Class<?>[] paramTypes = subCommand.getParameterTypes();
            Object[] paramValues = new Object[paramTypes.length];
            Annotation[][] paramAnnotations = subCommand.getParameterAnnotations();


            for (int i = 0; i < paramTypes.length; i++) {

                boolean isOptional = false;
                boolean isJoin = false;

                for (Annotation annotation : paramAnnotations[i]) {
                    if (annotation.annotationType() == Optional.class) {
                        isOptional = true;

                    } else if(annotation.annotationType() == Join.class) {
                        isJoin = true;

                    }
                }

                if(i + 1 == paramTypes.length && paramTypes[i] == String.class && isJoin) {

                    String concatenated = "";

                    if(i + 1 >= args.length) {
                        concatenated = null;
                    } else {
                        for (int y = i + 1; y < args.length; y++) {
                            if (y + 1 != args.length) {
                                concatenated = concatenated + args[y] + " ";
                            } else {
                                concatenated = concatenated + args[y];
                            }
                        }
                    }

                    paramValues[i] = concatenated;

                }

                if (i + 1 < args.length) {


                    if (paramTypes[i] == String.class && !isJoin) {
                        paramValues[i] = args[i + 1];
                    } else if (paramTypes[i] == Integer.class) {
                        try {
                            paramValues[i] = Integer.parseInt(args[i + 1]);
                        } catch (NumberFormatException e) {

                            this.expectedArgumentType = "Integer";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == Double.class) {
                        try {
                            paramValues[i] = Double.parseDouble(args[i + 1]);
                        } catch (NumberFormatException e) {
                            this.expectedArgumentType = "Double";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == Float.class) {
                        try {
                            paramValues[i] = Float.parseFloat(args[i + 1]);
                        } catch(NumberFormatException e) {
                            this.expectedArgumentType = "Float";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == Material.class) {
                        if(Material.getMaterial(args[i + 1]) == null) {
                            this.expectedArgumentType = "Material";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        } else {
                            paramValues[i] = Material.getMaterial(args[i + 1]);
                        }
                    } else if (paramTypes[i] == World.class) {
                        if(Bukkit.getWorld(args[i + 1]) == null) {
                            this.expectedArgumentType = "World";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        } else {
                            paramValues[i] = Bukkit.getWorld(args[i + 1]);
                        }
                    } else if (paramTypes[i] == Player.class) {
                        if(Bukkit.getPlayer(args[i + 1]) == null) {
                            this.expectedArgumentType = "Player";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        } else {
                            paramValues[i] = Bukkit.getPlayer(args[i + 1]);
                        }
                    } else if (paramTypes[i] == OfflinePlayer.class) {
                        paramValues[i] = Bukkit.getOfflinePlayer(args[i + 1]);
                    } else if (paramTypes[i] == Boolean.class) {
                        paramValues[i] = Boolean.parseBoolean(args[i + 1]);
                    } else if (paramTypes[i].isEnum()) {
                        try {
                            paramValues[i] = Enum.valueOf((Class<Enum>) paramTypes[i], args[i + 1].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = paramTypes[i].getSimpleName();
                            this.expectedArgumentIndex = i;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == UUID.class) {
                        try {
                            paramValues[i] = UUID.fromString(args[i + 1]);
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = "UUID";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == PotionEffectType.class) {
                        PotionEffectType effect = PotionEffectType.getByName(args[i + 1].toUpperCase());
                        if (effect == null) {
                            this.expectedArgumentType = "Potion Effect";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                        paramValues[i] = effect;
                    } else if (paramTypes[i] == Enchantment.class) {
                        Enchantment enchantment = Enchantment.getByName(args[i + 1].toUpperCase());
                        if (enchantment == null) {
                            this.expectedArgumentType = "Enchantment";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                        paramValues[i] = enchantment;
                    } else if (paramTypes[i] == Sound.class) {
                        try {
                            paramValues[i] = Sound.valueOf(args[i + 1].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = "Sound";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == ChatColor.class) {
                        try {
                            paramValues[i] = ChatColor.valueOf(args[i + 1].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = "Color";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == Difficulty.class) {
                        try {
                            paramValues[i] = Difficulty.valueOf(args[i + 1].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = "Difficulty";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == GameMode.class) {
                        try {
                            paramValues[i] = GameMode.valueOf(args[i + 1].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = "GameMode";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == Biome.class) {
                        Biome biome = Biome.valueOf(args[i + 1].toUpperCase());
                        if (biome == null) {
                            this.expectedArgumentType = "Biome";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                        paramValues[i] = biome;
                    } else if (paramTypes[i] == WeatherType.class) {
                        try {
                            paramValues[i] = WeatherType.valueOf(args[i + 1].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = "Weather";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    } else if (paramTypes[i] == EntityType.class) {
                        try {
                            paramValues[i] = EntityType.valueOf(args[i + 1].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            this.expectedArgumentType = "Entity Type";
                            this.expectedArgumentIndex = i+1;
                            invalidUsage();
                            sender.sendMessage(buildInvalidArgumentMessage(command, args));
                            return true;
                        }
                    }

                    if (paramValues[i] == null) {
                        return true;
                    }
                } else if (isOptional) {

                    paramValues[i] = getDefaultValue(paramTypes[i]);
                } else {

                    this.expectedArgumentIndex = i+1;
                    missingRequiredArgument();
                    return true;
                }

            }

            try {
                subCommand.invoke(this, paramValues);
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage("§4§lCatalyst Error: §r§cCould not execute the subcommand, see console for more. Please report this to a system administrator.");
            }
        } else {
            // Handle unknown subcommand
            if(sender instanceof Player) {
                ((Player) sender).performCommand(command.getName());
            } else {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /" + label + " for help.");
            }

        }

        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        List<String> completions = new ArrayList<>();

        if(subCommands.isEmpty()) {

            if (indexMethod != null) {

                Annotation[][] paramAnnotations = indexMethod.getParameterAnnotations();


                int argIndex = args.length - 1;

                if (argIndex < paramAnnotations.length) {
                    for (Annotation annotation : paramAnnotations[argIndex]) {
                        if (annotation instanceof AutoComplete) {
                            String autoCompleteValue = ((AutoComplete) annotation).value();

                            if (autoCompleteValue.equalsIgnoreCase("#onlineplayers")) {
                                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                    completions.add(onlinePlayer.getName());
                                }
                            } else if (autoCompleteValue.equalsIgnoreCase("#materials")) {
                                for (Material material : Material.values()) {
                                    completions.add(material.name());
                                }
                            } else if (autoCompleteValue.startsWith("#range:")) {
                                String[] split = autoCompleteValue.split(":");
                                String[] range = split[1].split("-");

                                for(int x = Integer.parseInt(range[0]); x <= Integer.parseInt(range[1]); x++) {
                                    completions.add(String.valueOf(x));
                                }
                            } else if (autoCompleteValue.startsWith("#enum:")) {
                                String enumName = autoCompleteValue.substring("#enum:".length());
                                try {
                                    Class<?> enumClass = Class.forName(enumName);
                                    if (enumClass.isEnum()) {
                                        for (Object enumConstant : enumClass.getEnumConstants()) {
                                            completions.add(enumConstant.toString());
                                        }
                                    } else {

                                        sender.sendMessage("§4§lCatalyst Error: §r§c"+enumName+" is not an Enum, please check tab completion rules. Please report this to a system administrator.");
                                    }
                                } catch (ClassNotFoundException e) {
                                    sender.sendMessage("§4§lCatalyst Error: §r§cThe enum "+enumName+" can't be found, please check tab completion rules. Please report this to a system administrator.");
                                }
                            }



                            break;
                        }
                    }
                }
            }

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .collect(Collectors.toList());

        }

        // Handle subcommands
        if (args.length == 0 || args.length == 1) {

            return new ArrayList<>(subCommands.keySet());
        } else if (args.length > 1) {




            Method subCommand = subCommands.get(args[0]);

            if (subCommand != null) {

                Annotation[][] paramAnnotations = subCommand.getParameterAnnotations();


                int argIndex = args.length - 2;

                if (argIndex < paramAnnotations.length) {
                    for (Annotation annotation : paramAnnotations[argIndex]) {
                        if (annotation instanceof AutoComplete) {
                            String autoCompleteValue = ((AutoComplete) annotation).value();

                            if (autoCompleteValue.equalsIgnoreCase("#onlineplayers")) {
                                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                    completions.add(onlinePlayer.getName());
                                }
                            } else if (autoCompleteValue.equalsIgnoreCase("#materials")) {
                                for (Material material : Material.values()) {
                                    completions.add(material.name());
                                }
                            } else if (autoCompleteValue.startsWith("#range:")) {
                                String[] split = autoCompleteValue.split(":");
                                String[] range = split[1].split("-");

                                for(int x = Integer.parseInt(range[0]); x <= Integer.parseInt(range[1]); x++) {
                                    completions.add(String.valueOf(x));
                                }
                            } else if (autoCompleteValue.startsWith("#enum:")) {
                                String enumName = autoCompleteValue.substring("#enum:".length());
                                try {
                                    Class<?> enumClass = Class.forName(enumName);
                                    if (enumClass.isEnum()) {
                                        for (Object enumConstant : enumClass.getEnumConstants()) {
                                            completions.add(enumConstant.toString());
                                        }
                                    } else {
                                        sender.sendMessage("§4§lCatalyst Error: §r§c"+enumName+" is not an Enum, please check tab completion rules. Please report this to a system administrator.");
                                    }
                                } catch (ClassNotFoundException e) {
                                    sender.sendMessage("§4§lCatalyst Error: §r§cThe enum "+enumName+" can't be found, please check tab completion rules. Please report this to a system administrator.");
                                }
                            }



                            break;
                        }
                    }
                }
            }
        }


        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }



    public String getCommandName() {

        com.reigindustries.catalyst.command.factory.annotations.Command command = this.getClass().getAnnotation(com.reigindustries.catalyst.command.factory.annotations.Command.class);
        if (command != null) {
            return command.value().split("\\|")[0];
        }
        return null;
    }


    protected CommandSender getSender() {
        return sender;
    }

    protected Player getPlayer() {
        return player;
    }

    protected List<String> getArgs() {
        return args;
    }

    // ${command}, ${type}, ${parameter}

    public void noPermission() {
        getSender().sendMessage(Config.get(Option.COMMAND_NO_PERMISSION_MESSAGE).replace("${command}", getCommandName()));
    }

    public void invalidUsage() {
        getSender().sendMessage(Config.get(Option.COMMAND_INVALID_PARAMETER_MESSAGE).replace("${command}", getCommandName()).replace("${type}", this.expectedArgumentType).replace("${parameter}", String.valueOf(this.expectedArgumentIndex)));
    }

    public void requiresPlayer() {
        getSender().sendMessage(Config.get(Option.COMMAND_REQUIRES_PLAYER_MESSAGE).replace("${command}", getCommandName()));
    }

    public void missingRequiredArgument() {
        getSender().sendMessage(Config.get(Option.COMMAND_MISSING_REQUIRED_PARAMETER_MESSAGE).replace("${command}", getCommandName()).replace("${parameter}", String.valueOf(this.expectedArgumentIndex)));
    }




    private String buildInvalidArgumentMessage(Command command, String[] args) {
        String message = "§7/" + command.getName() + " ";
        int index = 0;
        for(String arg : args) {
            if(this.expectedArgumentIndex == index) {
                message += ChatColor.RED + "" + ChatColor.UNDERLINE + arg + " " + ChatColor.RESET;
            } else {
                message += ChatColor.GRAY + arg + " " + ChatColor.RESET;
            }
            index++;
        }

        return message;
    }



    private Object getDefaultValue(Class<?> paramType) {
        if (paramType == String.class) {
            return "";
        } else if (paramType == Integer.class) {
            return 1;
        } else if (paramType == Double.class) {
            return 1.0;
        } else if (paramType == Boolean.class) {
            return false;
        }
        return null;
    }


}