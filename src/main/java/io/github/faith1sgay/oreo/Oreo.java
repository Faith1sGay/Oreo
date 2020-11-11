package io.github.faith1sgay.oreo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoCredential;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.user.Presence;
import com.mewna.catnip.shard.DiscordEvent;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonArray;
import com.mongodb.client.MongoClients;
import org.bson.Document;
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
        commands.put("mongoTest", Oreo::MongoTest);

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
    public static void MongoTest(@Nonnull Context context)
    {
        context.message().channel().sendMessage(
                "Inserting into database..."
        );
        MongoCredential credential = MongoCredential.createCredential("root", "admin", "secret".toCharArray());
        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress("mongodb"))))
                        .credential(credential)
                        .build());
        MongoDatabase database = mongoClient.getDatabase("Oreo");
        MongoCollection<Document> collection = database.getCollection("Example");
        Document doc = new Document("name", "MongoDB")
                .append("type", "database")
                .append("count", 1)
                .append("versions", Arrays.asList("v3.2", "v3.0", "v2.6"))
                .append("info", new Document("x", 203).append("y", 102));
        try {
            collection.insertOne(doc);
            context.message().channel().sendMessage("Successfully inserted!");
        }
        catch (Exception e)
        {
            context.message().channel().sendMessage("OOPS, something went wrong: \n"+ e);
        }

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
        Ready(catnip);


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
    private static void Ready(Catnip catnip) {
        catnip.observable(DiscordEvent.READY).subscribe(ready -> {
            catnip.presence(Presence.of(Presence.OnlineStatus.ONLINE, Presence.Activity.of("Milk's Favorite Cookie!", Presence.ActivityType.PLAYING)));
        });
    }


}

