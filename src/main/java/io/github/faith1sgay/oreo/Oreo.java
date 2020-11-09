package io.github.faith1sgay.oreo;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.shard.DiscordEvent;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.io.InputStream;

public class Oreo {
    private final List<String> prefixes;
    private final Catnip catnip;
    private final Trie commands;

    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger("io.github.faith1sgay.oreo.Oreo");

        // load configuration
        InputStream configStream = Oreo.class.getResourceAsStream("/config.json");
        if (configStream == null) {
            logger.error("You need to fill out config.example.json and move it to config.json!");
            return;
        }

        JsonObject config;
        try {
            config = JsonParser.object().from(configStream);
        } catch (JsonParserException e) {
            logger.error("Invalid config!", e);
            return;
        }

        logger.info("Loaded configuration!");

        String token = (String) config.get("token");
        String[] prefixes = ((JsonArray) config.get("prefixes")).toArray(new String[0]);

        // example commands
        HashMap<String, Consumer<Context>> commands = new HashMap<>();
        commands.put("example", Oreo::example);
        commands.put("example test test", Oreo::exampleTest);
        commands.put("help", Oreo::help);

        Oreo bot = new Oreo(prefixes, token, new Trie(commands));
        logger.info("Starting bot!");

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

