package io.github.faith1sgay.oreo;

import com.mewna.catnip.entity.message.Message;

public class Context {
    private final String prefix;
    private String command;
    private String arguments;
    private final Message message;

    public Context(String prefix, String command, String arguments, Message message) {
        this.prefix = prefix;
        this.command = command;
        this.arguments = arguments;
        this.message = message;
    }

    public String prefix() {
        return this.prefix;
    }

    public String command() {
        return this.command;
    }

    public String arguments() {
        return this.arguments;
    }

    public Message message() {
        return this.message;
    }

    public void addCommand(String appended) {
        if (this.command.equals("")) {
            this.command += appended;
        } else {
            this.command += " " + appended;
        }
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
}
