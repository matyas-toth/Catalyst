package com.reigindustries.catalyst.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.function.Consumer;

public class CoreCommand extends Command {

    private final Consumer<CommandContext> handler;

    public CoreCommand(String name, Consumer<CommandContext> handler) {
        super(name);
        this.handler = handler;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        CommandContext context = new CommandContext(sender, args);
        handler.accept(context);
        return true;
    }

    public static class CommandContext {
        private final CommandSender sender;
        public final String[] args;

        public CommandContext(CommandSender sender, String[] args) {
            this.sender = sender;
            this.args = args;
        }

        public CommandSender getSender() {
            return sender;
        }

        public boolean isPlayer() {
            return sender instanceof org.bukkit.entity.Player;
        }

        public org.bukkit.entity.Player getPlayer() {
            return (org.bukkit.entity.Player) sender;
        }
    }
}