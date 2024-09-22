package com.reigindustries.catalystexample.commands;

import com.reigindustries.catalyst.command.factory.ComplexCommand;
import com.reigindustries.catalyst.command.factory.annotations.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Command("mycommand|mycmd")
@Permission("base.cmd.example")
public class MyCommand extends ComplexCommand {

    @Override
    public void index() {
        getSender().sendMessage("This is the base command, no subcommand was used.");
    }

    @Override
    public void noPerm() {
        getSender().sendMessage("You do not have permission to execute this command.");
    }

    @Subcommand("greet|gr")
    @Permission("base.cmd.example.greet")
    public void greetSubCommand(String name, @Optional String message) {
        if (message == null || message.isEmpty()) {
            message = "Welcome!";
        }
        getSender().sendMessage("Greeting " + name + ": " + message);
    }

    @Subcommand("add")
    @Permission("base.cmd.example.add")
    public void addSubCommand(Integer a, @Optional Integer b) {
        if (b == null) {
            b = 0;
        }
        int sum = a + b;
        getSender().sendMessage("The sum of " + a + " and " + b + " is: " + sum);
    }

    @Subcommand("info")
    @Permission("base.cmd.example.info")
    public void infoSubCommand(@AutoComplete("#enum:com.reigindustries.catalystexample.commands.MyEnum") @Optional Player target) {
        if (target == null) {
            getSender().sendMessage("This is general info about the server.");
        } else {
            getSender().sendMessage("Information about player: " + target.getName());
        }
    }

    @Subcommand("getmaterial|getmat|mat")
    public void getMaterial(@AutoComplete("#materials") Material material, @AutoComplete("#range:1-64") @Optional Integer number) {
        getPlayer().getInventory().addItem(new ItemStack(material, number));
        getPlayer().updateInventory();
    }
}