package Oreo;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.shard.DiscordEvent;

import java.util.Arrays;
import java.util.Comparator;

public class Oreo {
    final private String[] prefixes;
    final private Catnip catnip;

    public static void main(String[] args) {
        String[] prefixes = {"~", "!", "~~"};
        String token = "your token here";

        Oreo bot = new Oreo(prefixes, token);
        bot.run();
    }

    public Oreo(String[] prefixes, String token) {
        Arrays.sort(prefixes, Comparator.comparing(String::length));
        this.prefixes = reverseArray(prefixes);
        this.catnip = Catnip.catnip(token);

        catnip.observable(DiscordEvent.MESSAGE_CREATE)
                .subscribe(this::handleMessage);
    }

    private void run() {
        catnip.connect();
    }

    private String deprefixify(String command) {
        for (String prefix : this.prefixes) {
            if (command.startsWith(prefix)) {
                return prefix;
            }
        }
        // sentinel value. we assume no blank prefix
        return "";
    }


    private void handleMessage(Message message) {
        String prefix = deprefixify(message.content());

        if (prefix.equals("")) return;

        String command = message.content().substring(prefix.length());

        switch (command) {
            // commands go here.
        }
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

