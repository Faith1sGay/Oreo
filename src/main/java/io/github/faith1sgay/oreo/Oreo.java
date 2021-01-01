package io.github.faith1sgay.oreo;

import com.mewna.catnip.CatnipOptions;
import com.mewna.catnip.extension.AbstractExtension;
import com.mewna.catnip.shard.GatewayIntent;
import com.mongodb.MongoCredential;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.user.Presence;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonArray;
import com.mongodb.client.MongoClients;
import io.github.faith1sgay.oreo.command_handler.CommandHandler;
import io.github.faith1sgay.oreo.command_handler.Context;
import io.github.faith1sgay.oreo.verification.VerificationExtension;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.util.*;
import java.io.InputStream;

public class Oreo {
    private final Catnip catnip;

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
        List<String> prefixes = Arrays.asList(((JsonArray) config.get("prefixes")).toArray(new String[0]));

        // example commands
        NotesCommand notes = new NotesCommand();
        CommandHandler commands = new CommandHandler(prefixes);
        commands.register("example", Oreo::example);
        commands.register("example test test", Oreo::exampleTest);
        commands.register("help", Oreo::help);
        commands.register("mongoTest", Oreo::MongoTest);
        commands.register("notes", notes::getNotes);

        VerificationExtension verificationExtension = new VerificationExtension(config);

        Oreo bot = new Oreo(prefixes, token, commands, verificationExtension);
        logger.info("Starting bot!");
        bot.run();
    }

    public Oreo(@Nonnull List<String> prefixes,
                @Nonnull String token,
                @Nonnull CommandHandler commands,
                @Nonnull VerificationExtension verificationExtension
    ) {
        prefixes.sort(Comparator.comparing(String::length));
        Collections.reverse(prefixes);

        this.catnip = Catnip.catnip(new CatnipOptions(token)
                .intents(Set.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILDS, GatewayIntent.GUILD_MESSAGE_REACTIONS))
                .initialPresence(Presence.of(Presence.OnlineStatus.ONLINE, Presence.Activity.of("Milk Cookies", Presence.ActivityType.COMPETING))));

        AbstractExtension commandsExtension = commands.toExtension();
        catnip.loadExtension(commandsExtension).loadExtension(verificationExtension);
    }

    public static void example(@Nonnull Context context) {
        context.message().respond("Wow");
    }

    public static void exampleTest(@Nonnull Context context) {
        context.message().respond(
                "Prefix: " + context.prefix() + "\n" +
                "Command: " + context.command() + "\n" +
                "Arguments: " + context.arguments());
    }

    public static void MongoTest(@Nonnull Context context)
    {
        context.message().respond(
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
            context.message().respond("Successfully inserted!");
        }
        catch (Exception e) {
            context.message().respond("OOPS, something went wrong: \n" + e);
        }

    }

    public static void help(@Nonnull Context context)
    {
        context.message().respond(
                "Oreo help\n" +
                        "example\n" +
                        "exampleTest\n" +
                        "notes <bot id>"
        );
    }

    private void run() {
        catnip.connect();
    }
}

