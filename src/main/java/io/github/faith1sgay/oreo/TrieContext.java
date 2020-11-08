package io.github.faith1sgay.oreo;

import com.mewna.catnip.entity.message.Message;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class TrieContext {
    private final String command;
    private final String arguments;
    private final Consumer<Context> consumer;

    public TrieContext(@Nonnull String command, @Nonnull String arguments, @Nonnull Consumer<Context> consumer) {
        this.command = command;
        this.arguments = arguments;
        this.consumer = consumer;
    }

    public Context evolve(String prefix, Message message) {
        return new Context(prefix, this.command, this.arguments, message);
    }

    public TrieContext evolve(String command, String spaces) {
        if (this.command.equals("")) {
            return new TrieContext(command, this.arguments, this.consumer);
        } else {
            return new TrieContext(command + spaces + this.command, this.arguments, this.consumer);
        }
    }

    public Consumer<Context> consumer() {
        return this.consumer;
    }
}
