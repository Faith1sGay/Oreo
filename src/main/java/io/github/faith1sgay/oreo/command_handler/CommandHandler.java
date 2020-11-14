package io.github.faith1sgay.oreo.command_handler;


import com.mewna.catnip.extension.AbstractExtension;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class CommandHandler {
    private final HashMap<String, Consumer<Context>> commands;
    private final List<String> prefixes;

    public CommandHandler(List<String> prefixes) {
        this.commands = new HashMap<>();
        this.prefixes = prefixes;
    }

    public void register(String command, Consumer<Context> func) {
        this.commands.put(command, func);
    }

    public AbstractExtension toExtension() {
        return new CommandHandlerExtension(this.prefixes, this.commands);
    }
}
