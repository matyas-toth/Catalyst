package com.reigindustries.catalyst.command.factory;

import com.reigindustries.catalyst.command.factory.annotations.*;
import com.reigindustries.catalyst.command.factory.parsing.ParameterParseException;
import com.reigindustries.catalyst.command.factory.parsing.ParameterResolver;
import com.reigindustries.catalyst.command.factory.tab.TabCompletionResolver;
import com.reigindustries.catalyst.internal.config.Config;
import com.reigindustries.catalyst.internal.config.Option;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;

public abstract class ComplexCommand implements CommandExecutor, TabCompleter {

    private CommandSender sender;
    private Player player;
    private List<String> args = new ArrayList<>();
    private final Map<String, Method> subCommands = new HashMap<>();
    private Method indexMethod = null;
    protected String expectedArgumentType;
    protected Integer expectedArgumentIndex;
    private final ParameterResolver parameterResolver = new ParameterResolver();
    private final TabCompletionResolver tabResolver = new TabCompletionResolver();




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
            if(getClass().isAnnotationPresent(Permission.class)) {
                if (!sender.hasPermission(getClass().getAnnotation(Permission.class).value())) {
                    noPermission();
                    return true;
                }
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

            Object[] paramValues;
            try {
                paramValues = parameterResolver.resolveForIndex(indexMethod, args);
            } catch (ParameterParseException e) {
                if (e.getKind() == ParameterParseException.ErrorKind.INVALID) {
                    this.expectedArgumentType = e.getExpectedType();
                    this.expectedArgumentIndex = e.getIndex();
                    invalidUsage();
                    sender.sendMessage(buildInvalidArgumentMessage(command, args));
                } else {
                    this.expectedArgumentIndex = e.getIndex();
                    missingRequiredArgument();
                }
                return true;
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
            if(getClass().isAnnotationPresent(Permission.class)) {
                if (!sender.hasPermission(getClass().getAnnotation(Permission.class).value())) {
                    noPermission();
                    return true;
                }
            }
        } else {
            // Handle non-player command senders if necessary
        }

        String subcommandName = args[0];
        Method subCommand = subCommands.get(subcommandName);

        if (subCommand != null) {

            if(subCommand.isAnnotationPresent(Permission.class)) {
                if(!sender.hasPermission(subCommand.getAnnotation(Permission.class).value())) {
                    noPermission();
                    return true;
                }
            }

            Object[] paramValues;
            try {
                paramValues = parameterResolver.resolveForSub(subCommand, args);
            } catch (ParameterParseException e) {
                if (e.getKind() == ParameterParseException.ErrorKind.INVALID) {
                    this.expectedArgumentType = e.getExpectedType();
                    this.expectedArgumentIndex = e.getIndex();
                    invalidUsage();
                    sender.sendMessage(buildInvalidArgumentMessage(command, args));
                } else {
                    this.expectedArgumentIndex = e.getIndex();
                    missingRequiredArgument();
                }
                return true;
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

        if (subCommands.isEmpty()) {
            return tabResolver.completeForIndex(indexMethod, args);
        }

        // Handle subcommands
        if (args.length == 0 || args.length == 1) {

            return new ArrayList<>(subCommands.keySet());
        } else if (args.length > 1) {




            Method subCommand = subCommands.get(args[0]);
            if (subCommand != null) {
                return tabResolver.completeForSub(subCommand, args);
            }
        }
        return Collections.emptyList();
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
                message += ChatColor.RED + "" + ChatColor.UNDERLINE + arg + ChatColor.RESET + " ";
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