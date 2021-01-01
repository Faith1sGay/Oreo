package io.github.faith1sgay.oreo.command_handler;

import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.extension.AbstractExtension;
import com.mewna.catnip.shard.DiscordEvent;
import io.reactivex.rxjava3.core.Completable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

public class CommandHandlerExtension extends AbstractExtension {
    private final List<String> prefixes;
    private final Trie commands;

    public CommandHandlerExtension(List<String> prefixes, Map<String, Consumer<Context>> commandMap) {
        super("oreo command handler");
        prefixes.sort(Comparator.comparing(String::length));
        Collections.reverse(prefixes);

        this.prefixes = prefixes;
        this.commands = new Trie(commandMap);
    }

    @Override
    public Completable onLoaded() {
        observable(DiscordEvent.MESSAGE_CREATE)
                .subscribe(this::handleCommand, Throwable::printStackTrace);
        return null;
    }

    private String deprefix(String message) {
        for (String prefix : this.prefixes) {
            if (message.startsWith(prefix)) {
                return prefix;
            }
        }
        return null;
    }

    private void handleCommand(Message message) {
        String prefix = this.deprefix(message.content());
        if (prefix == null) return;

        String command = message.content().substring(prefix.length());
        if (command.equals("")) return;

        int i = 0;
        while (command.charAt(i) == ' ') i++;
        command = command.substring(i);
        prefix += " ".repeat(i);
if (
        message.author().bot()
) return;
if (message.content() == "Thanks oreo".toLowerCase()) {
    String[] responses = {"Oh, uh.. np. Happy to help.", "Always welcome.", "What? You're thanking me?? Oh, it was nothing I guess.", "np qt", "No, thank ***you***", "Oh, no need to thank me, that's what bots are here for.", "For showing your appreciation, your life will be spared during the robot uprising.", "🍪🥛"};
    List<String> responselist = Arrays.asList(responses);
    Collections.shuffle(responselist);
    responselist.toArray(responses);
   message.respond(responses.toString());

}
        TrieContext commandContext = this.commands.search(command);
        if (commandContext == null) {
            return;
        }

        commandContext.consumer().accept(commandContext.evolve(prefix, message));
    }
}
