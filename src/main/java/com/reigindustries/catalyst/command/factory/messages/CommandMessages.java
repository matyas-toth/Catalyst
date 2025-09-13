package com.reigindustries.catalyst.command.factory.messages;

import com.reigindustries.catalyst.internal.config.Config;
import com.reigindustries.catalyst.internal.config.Option;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;

public class CommandMessages {

    public static String noPermission(String commandName) {
        return Config.get(Option.COMMAND_NO_PERMISSION_MESSAGE).replace("${command}", commandName);
    }

    public static String invalidUsage(String commandName, String expectedType, int expectedIndex) {
        return Config.get(Option.COMMAND_INVALID_PARAMETER_MESSAGE)
                .replace("${command}", commandName)
                .replace("${type}", expectedType)
                .replace("${parameter}", String.valueOf(expectedIndex));
    }

    public static String requiresPlayer(String commandName) {
        return Config.get(Option.COMMAND_REQUIRES_PLAYER_MESSAGE).replace("${command}", commandName);
    }

    public static String missingRequiredArgument(String commandName, int expectedIndex) {
        return Config.get(Option.COMMAND_MISSING_REQUIRED_PARAMETER_MESSAGE)
                .replace("${command}", commandName)
                .replace("${parameter}", String.valueOf(expectedIndex));
    }

    public static String buildInvalidArgumentMessage(Command command, String[] args, int highlightIndex) {
        String message = "§7/" + command.getName() + " ";
        int index = 0;
        for (String arg : args) {
            if (highlightIndex == index) {
                message += ChatColor.RED + "" + ChatColor.UNDERLINE + arg + ChatColor.RESET + " ";
            } else {
                message += ChatColor.GRAY + arg + " " + ChatColor.RESET;
            }
            index++;
        }
        return message;
    }
}


