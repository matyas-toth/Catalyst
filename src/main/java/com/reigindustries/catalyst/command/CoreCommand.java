package com.reigindustries.catalyst.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CoreCommand extends Command {

    private final Consumer<CommandContext> handler;
    private Function<CommandContext, List<String>> tabCompleter;

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

    public void setTabCompleter(Function<CommandContext, List<String>> tabCompleter) {
        this.tabCompleter = tabCompleter;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        CommandContext context = new CommandContext(sender, args);
        if (tabCompleter != null) {
            return tabCompleter.apply(context);
        }
        return List.of(); // Return an empty list if no tab completer is set
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