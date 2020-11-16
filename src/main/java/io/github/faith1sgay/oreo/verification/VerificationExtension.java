package io.github.faith1sgay.oreo.verification;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.guild.PermissionOverride;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.util.Permission;
import com.mewna.catnip.extension.AbstractExtension;
import com.mewna.catnip.shard.DiscordEvent;
import io.reactivex.rxjava3.core.Completable;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class VerificationExtension extends AbstractExtension {
    // channel id to the sandbox.
    private final HashMap<String, Sandbox> sandboxes;

    public VerificationExtension(JsonObject configuration) {
        super("verification channel watcher");
        final Logger logger = LoggerFactory.getLogger("io.github.faith1sgay.oreo.verification" +
                ".verification_extension");
        String[] verificationChannels = ((JsonArray) configuration.get("sandboxes")).toArray(new String[0]);
        JsonObject relatedGuilds = configuration.getObject("guilds");

        if (relatedGuilds == null) {
            logger.error("'guilds' config value is not set up.");
            relatedGuilds = new JsonObject();
        }

        this.sandboxes = new HashMap<>();

        for (String verificationChannel : verificationChannels) {
            String guildId = relatedGuilds.getString(verificationChannel);

            if (guildId == null) {
                logger.error("'guilds' config value is not set up.");
                continue;
            }

            this.sandboxes.put(verificationChannel, new Sandbox(verificationChannel, guildId));
        }
    }

    @Override
    public Completable onLoaded() {
        observable(DiscordEvent.MESSAGE_CREATE)
                .subscribe(this::updateLatestMsg);
        observable(DiscordEvent.MESSAGE_CREATE)
                .subscribe(this::getNewNote);
        observable(DiscordEvent.CHANNEL_UPDATE)
                .subscribe(this::updateSandboxes);

        return null;
    }


    private void getNewNote(Message message) {
        // we aren't looking for any bot msgs
        Sandbox sandbox = this.sandboxes.get(message.channelId());
        if (message.author().bot()) return;
        if (sandbox == null) return;

        String self = message.catnip().clientId();

        int mentionLength = 0;
        if (message.content().startsWith("<@!" + self + ">"))
            mentionLength = ("<@!" + self + ">").length();
        if (message.content().startsWith("<@" + self + ">"))
            mentionLength = ("<@" + self + ">").length();

        if (mentionLength == 0) return;

        String note = message.content().substring(mentionLength + 1);

        String lastCommand = sandbox.lastBotCommand();

        if (lastCommand == null) {
            message.catnip().rest().channel().createMessage(message.channelId(), "No prefix found! What " +
                    "prefix does this bot have?");
            Message reply = message.catnip().observable(DiscordEvent.MESSAGE_CREATE)
                    .filter(msg -> msg.channelId().equals(message.channelId()))
                    .filter(msg -> msg.author().id().equals(message.author().id()))
                    .blockingFirst();

            sandbox.botPrefix(reply.content());

            message.catnip().rest().channel().createMessage(message.channelId(), "Could you rerun the bot's " +
                    "command and set the note again?");
        } else {
            message.catnip().rest().channel().createMessage(message.channelId(), note + "\n"
                    + sandbox.lastBotCommand());
        }
    }

    private void updateLatestMsg(Message message) {
        if (message.author().bot()) return;
        if (!this.sandboxes.containsKey(message.channelId())) return;

        this.sandboxes.get(message.channelId()).checkMessage(message.content(), message.id());
    }

    private void updateSandboxes(Pair<Channel, Channel> revisions) {
        Channel channel = revisions.getRight();
        Sandbox sandbox = this.sandboxes.get(channel.id());
        if (sandbox == null) return;
        if (!channel.isGuild()) return;

        List<PermissionOverride> overrides = channel.asGuildChannel().overrides();

        PermissionOverride wantedOverride = null;
        for (PermissionOverride override : overrides) {
            // we're looking for an override that only affects a user (a bot) and which allows sending + deleting.
            if (override.type() == PermissionOverride.OverrideType.MEMBER &&
                    override.allow().contains(Permission.SEND_MESSAGES) &&
                    override.allow().contains(Permission.MANAGE_MESSAGES) &&
                    override.allow().contains(Permission.VIEW_CHANNEL)) {
                wantedOverride = override;
                break;
            }
        }

        if (wantedOverride == null) {
            sandbox.clearBot();
            return;
        }

        sandbox.newBot(wantedOverride.id());
        // sandbox.newBot("241930933962407936");
        channel.asTextChannel().sendMessage(Objects.requireNonNull(sandbox.botPrefix()));
    }
}
