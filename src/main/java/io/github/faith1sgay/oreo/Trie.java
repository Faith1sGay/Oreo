package io.github.faith1sgay.oreo;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Trie {
    private final Map<String, Trie> commands;
    private final Consumer<Context> consumer;

    public Trie(@Nonnull Map<String, Consumer<Context>> commands) {
        this.consumer = commands.get("");
        HashMap<String, HashMap<String, Consumer<Context>>> iteratedMap = new HashMap<>();
        for (Map.Entry<String, Consumer<Context>> commandEntry : commands.entrySet()) {
            String command = commandEntry.getKey();
            if (command.equals("")) {
                continue;
            }
            String start = null;
            String newCommand = null;
            for (int i = 0; i < command.length(); i++) {
                if (command.charAt(i) == ' ') {
                    start = command.substring(0, i);
                    newCommand = command.substring(i + 1);
                    break;
                }
            }
            if (start == null) {
                start = command;
                newCommand = "";
            }

            if (!iteratedMap.containsKey(start)) {
                iteratedMap.put(start, new HashMap<>());
            }

            iteratedMap.get(start).put(newCommand, commandEntry.getValue());
        }

        HashMap<String, Trie> finalMap = new HashMap<>();
        for (Map.Entry<String, HashMap<String, Consumer<Context>>> entry : iteratedMap.entrySet()) {
            finalMap.put(entry.getKey(), new Trie(entry.getValue()));
        }

        this.commands = Map.copyOf(finalMap);
    }

    public void call(@Nonnull String command, @Nonnull Context context) {
        if (command.equals("")) {
            if (this.consumer != null) {
                context.setArguments(command);
                this.consumer.accept(context);
            }
            return;
        }

        String start = null;
        for (int i = 0; i < command.length(); i++) {
            if (command.charAt(i) == ' ') {
                start = command.substring(0, i);
                command = command.substring(i + 1);
                break;
            }
        }
        if (start == null) {
            start = command;
            command = "";
        }

        Trie nextTrie = this.commands.get(start);
        if (nextTrie == null) {
            if (this.consumer != null) {
                if (!command.equals("")) {
                    context.setArguments(start + " " + command);
                } else {
                    context.setArguments(start);
                }
                this.consumer.accept(context);
            }
            return;
        }

        context.addCommand(start);

        nextTrie.call(command, context);
    }
}
