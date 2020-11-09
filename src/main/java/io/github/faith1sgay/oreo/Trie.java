package io.github.faith1sgay.oreo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
            finalMap.put(entry.getKey().toLowerCase(), new Trie(entry.getValue()));
        }

        this.commands = Map.copyOf(finalMap);
    }

    @Nullable
    public TrieContext search(@Nonnull String command) {
        if (command.equals("")) {
            return this.maybeReturnContext("", "");
        }

        String start = null;
        int numberOfSpaces = 0;
        for (int i = 0; i < command.length(); i++) {
            if (command.charAt(i) == ' ') {
                start = command.substring(0, i);
                command = command.substring(i);
                numberOfSpaces = this.nextNonSpace(command);
                command = command.substring(numberOfSpaces);
                break;
            }
        }
        if (start == null) {
            start = command;
            numberOfSpaces = 0;
            command = "";
        }

        Trie nextTrie = this.commands.get(start.toLowerCase());

        if (nextTrie == null) {
            if (command.equals("")) {
                return this.maybeReturnContext("", start);
            } else {
                return this.maybeReturnContext("", start + " " + command);
            }
        }

        TrieContext nextResult = nextTrie.search(command);
        if (nextResult == null) {
            return this.maybeReturnContext(start, command);
        }

        return nextResult.evolve(start, nSpaces(numberOfSpaces));
    }

    @Nonnull
    private String nSpaces(int n) {
        StringBuilder output = new StringBuilder();
        while (n > 0) {
            output.append(" ");
            n--;
        }
        return output.toString();
    }

    @Nullable
    private TrieContext maybeReturnContext(@Nonnull String command, @Nonnull String arguments) {
        if (this.consumer == null) {
            return null;
        }

        return new TrieContext(command, arguments, this.consumer);
    }

    private int nextNonSpace(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) != ' ') {
                return i;
            }
        }
        return string.length(); // not really a sentinel but whatever...
    }
}
