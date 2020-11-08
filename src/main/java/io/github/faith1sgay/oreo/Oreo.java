package io.github.faith1sgay.oreo;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.shard.DiscordEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class Oreo {
    private final List<String> prefixes;
    private final Catnip catnip;
    private final Trie commands;

    public static void main(String[] args) {
        String[] prefixes = {"~", "!", "~~", "oreo "};
        String token = "";

        // example commands
        HashMap<String, Consumer<Context>> commands = new HashMap<>();
        commands.put("example", Oreo::example);
        commands.put("example test test", Oreo::exampleTest);
        commands.put("help", Oreo::help);

        Oreo bot = new Oreo(prefixes, token, new Trie(commands));
        bot.run();
    }

    public static void example(@Nonnull Context context) {
        context.message().channel().sendMessage("Wow");
    }

    public static void exampleTest(@Nonnull Context context) {
        context.message().channel().sendMessage(
                "Prefix: " + context.prefix() + "\n" +
                "Command: " + context.command() + "\n" +
                "Arguments: " + context.arguments());
    }
    public static void help(@Nonnull Context context)
    {
        context.message().channel().sendMessage(
                "Oreo help\n" +
                        "example\n"+
                        "exampleTest"
        );
    }

    public Oreo(String[] prefixes, String token, Trie commands) {
        List<String> prefixesList = Arrays.asList(prefixes);
        prefixesList.sort(Comparator.comparing(String::length));
        Collections.reverse(prefixesList);

        this.prefixes = prefixesList;
        this.commands = commands;
        this.catnip = Catnip.catnip(token);

        catnip.observable(DiscordEvent.MESSAGE_CREATE).subscribe(this::handleMessage);
    }

    private void run() {
        catnip.connect();
    }

    @Nullable
    private String deprefixify(String command) {
        for (String prefix : this.prefixes) {
            if (command.startsWith(prefix)) {
                return prefix;
            }
        }
        return null;
    }


    private void handleMessage(@Nonnull Message message) {
        String prefix = deprefixify(message.content());
        if (prefix == null) return;

        String command = message.content().substring(prefix.length());

        TrieContext commandContext = this.commands.search(command);
        if (commandContext == null) {
            return;
        }

        commandContext.consumer().accept(commandContext.evolve(prefix, message));
    }
}

