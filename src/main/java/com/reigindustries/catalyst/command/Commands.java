package com.reigindustries.catalyst.command;

import com.reigindustries.catalyst.autoload.CommandLoader;

public class Commands {

    public CatalystCommand on(String cmd) {
        return new CatalystCommand(cmd);
    }

    public void load(String... commandPackageNames) {
        CommandLoader loader = new CommandLoader();
        loader.registerCommands(commandPackageNames);
    }

}
