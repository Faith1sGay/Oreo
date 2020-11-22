package io.github.faith1sgay.oreo;

import com.mewna.catnip.entity.message.ReactionUpdate;
import com.mewna.catnip.shard.DiscordEvent;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import io.github.faith1sgay.oreo.command_handler.Context;
import io.reactivex.rxjava3.core.Observable;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotesCommand {
    private final MongoClient mongoClient;

    public NotesCommand() {
        MongoCredential credential = MongoCredential.createCredential(
                System.getenv("MONGO_USERNAME"),
                "admin",
                System.getenv("MONGO_PASSWORD").toCharArray()
        );

        this.mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(
                                        Collections.singletonList(new ServerAddress(System.getenv("MONGO_HOST")))
                                )
                        )
                        .credential(credential)
                        .build()
        );
    }

    public void getNotes(@Nonnull Context context) {
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("Oreo").getCollection("notes");

        // the bot id will be the argument
        if (context.arguments().strip().equals("") || context.arguments().contains(" ")) {
            context.message().reply("Usage: `" + context.prefix() + context.command() + " <bot id>`.");
            return;
        }

        String botId = context.arguments();
        FindIterable<Document> notes = mongoCollection.find(new Document("bot_id", botId));

        ArrayList<Pair<String, String>> properNotes = new ArrayList<>();

        notes.forEach(document ->
                properNotes.add(Pair.of(document.getString("note"), document.getString("last_message"))));

        Pair<String, Integer> result = buildMessage(properNotes, 0);
        int offset = result.getRight();

        if (result.getLeft().equals("")) {
            context.message().reply("That bot has no notes set!");
            return;
        }

        context.message().reply(result.getLeft()).subscribe(message -> {
            NoteResponse responder = new NoteResponse(offset, properNotes, message);
            message.react("⏮️")
                    .subscribe(() -> message.react("⏭"));

            List<Observable<ReactionUpdate>> observables = Arrays.asList(
                    message.catnip().observable(DiscordEvent.MESSAGE_REACTION_ADD),
                    message.catnip().observable(DiscordEvent.MESSAGE_REACTION_REMOVE)
            );
            Observable.merge(observables)
                    .filter(reactionUpdate -> reactionUpdate.userId() != null
                            && reactionUpdate.userId().equals(context.message().author().id())
                            && reactionUpdate.channelId().equals(message.channelId())
                            && (reactionUpdate.emoji().is("⏮️") || reactionUpdate.emoji().is("⏭")))
                    // limit so we don't run into the edit ratelimit
                    .throttleLatest(1, TimeUnit.SECONDS)
                    // todo: figure out how to delete the reactions after this timeout expires.
                    .timeout(5, TimeUnit.MINUTES)
                    .subscribe(responder::handleReaction, exception -> {});
        });

    }

    @Nonnull
    public static Pair<String, Integer> buildMessage(@Nonnull List<Pair<String, String>> notes, int offset) {
        if (offset >= notes.size()) return Pair.of("", offset);

        int length = 0;
        StringBuilder res = new StringBuilder();
        Pair<String, String> note = notes.get(offset);

        while (length + 8 + note.getRight().length() + note.getLeft().length() < 750) {
            res.append("**").append(note.getLeft()).append("**:\n").append(note.getRight()).append("\n\n");
            length += 8 + note.getRight().length() + note.getLeft().length();
            offset++;
            if (offset == notes.size()) return Pair.of(res.toString(), offset);
            note = notes.get(offset);
        }

        return Pair.of(res.toString(), offset);
    }
}
