package io.github.faith1sgay.oreo;

import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.extension.AbstractExtension;
import com.mewna.catnip.shard.DiscordEvent;
import io.reactivex.rxjava3.core.Completable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
        on(DiscordEvent.MESSAGE_CREATE, this::handleCommand);
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

        int i = 0;
        while (command.charAt(i) == ' ') i++;
        command = command.substring(i);
        prefix += " ".repeat(i);

        TrieContext commandContext = this.commands.search(command);
        if (commandContext == null) {
            return;
        }

        commandContext.consumer().accept(commandContext.evolve(prefix, message));
    }
}
