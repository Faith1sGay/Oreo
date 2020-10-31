package Oreo;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.shard.DiscordEvent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Oreo {
    final private String[] prefixes;
    final private Catnip catnip;
    final private Trie commands;

    public static void main(String[] args) {
        String[] prefixes = {"~", "!", "~~"};
        String token = "your token here";

        // example commands
        HashMap<String, Consumer<Message>> commands = new HashMap<>();
        commands.put("example", Oreo::example);

        Oreo bot = new Oreo(prefixes, token, new Trie(commands));
        bot.run();
    }

    public static void example(Message message) {
        message.channel().sendMessage("Wow");
    }

    public Oreo(String[] prefixes, String token, Trie commands) {
        Arrays.sort(prefixes, Comparator.comparing(String::length));
        this.prefixes = reverseArray(prefixes);
        this.commands = commands;
        this.catnip = Catnip.catnip(token);

        catnip.observable(DiscordEvent.MESSAGE_CREATE)
                .subscribe(this::handleMessage);
    }

    private void run() {
        catnip.connect();
    }

    @org.jetbrains.annotations.Nullable
    @org.jetbrains.annotations.Contract(pure = true)
    private String deprefixify(String command) {
        for (String prefix : this.prefixes) {
            if (command.startsWith(prefix)) {
                return prefix;
            }
        }
        return null;
    }


    private void handleMessage(Message message) {
        String prefix = deprefixify(message.content());
        if (prefix == null) return;

        String command = message.content().substring(prefix.length());

        this.commands.call(command, message);
    }

    // fckin java
    private static String[] reverseArray(String[] argument) {
        for (int i = 0; i < argument.length / 2; i++) {
            String temp = argument[i];
            argument[i] = argument[argument.length - i - 1];
            argument[argument.length - i - 1] = temp;
        }

        return argument;
    }
}

