package Oreo;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;

import com.koloboke.collect.impl.hash.Hash;
import com.mewna.catnip.entity.message.Message;

public class Trie {
    final private Map<String, Trie> commands;
    final private Consumer<Message> consumer;

    public Trie(Map<String, Consumer<Message>> commands) {
        this.consumer = commands.get("");
        HashMap<String, HashMap<String, Consumer<Message>>> iteratedMap = new HashMap<>();
        for (String command : commands.keySet()) {
            if (command.equals("")) {
                continue;
            }
            String start = null;
            String new_command = null;
            for (int i = 0; i < command.length(); i++) {
                if (command.charAt(i) == ' ') {
                    start = command.substring(0, i);
                    new_command = command.substring(i + 1);
                    break;
                }
            }
            if (start == null) {
                start = command;
                new_command = "";
            }

            if (!iteratedMap.containsKey(start)) {
                iteratedMap.put(start, new HashMap<>());
            }

            iteratedMap.get(start).put(new_command, commands.get(command));
        }

        HashMap<String, Trie> finalMap = new HashMap<>();
        for (String start : iteratedMap.keySet()) {
            finalMap.put(start, new Trie(iteratedMap.get(start)));
        }

        this.commands = Map.copyOf(finalMap);
    }

    public void call(String command, Message message) {
        if (command.equals("")) {
            if (this.consumer != null) {
                this.consumer.accept(message);
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
            return;
        }
        nextTrie.call(command, message);
    }
}
