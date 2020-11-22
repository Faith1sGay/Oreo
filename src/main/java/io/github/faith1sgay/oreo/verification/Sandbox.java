package io.github.faith1sgay.oreo.verification;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonObject;

public class Sandbox {
    private final String id;
    private final String guildId;
    private final HttpClient httpClient;
    private String lastBotCommand;
    private String botPrefix;
    private String botId;

    public Sandbox(String id, String guildId) {
        this.id = id;
        this.lastBotCommand = null;
        this.botPrefix = null;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        this.guildId = guildId;
    }

    public void checkMessage(String content, String id) {
        if (this.botPrefix == null) return;
        if (content.startsWith(this.botPrefix)) {
            this.lastBotCommand = id;
        }
    }

    public void newBot(String id) {
        this.lastBotCommand = null;
        this.botPrefix = null;
        this.botId = id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://discord.bots.gg/api/v1/bots/" + id))
                .build();
        HttpResponse<String> httpResponse = null;

        try {
            httpResponse = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        JsonObject response;
        if (httpResponse == null) {
            return;
        }

        try {
            response = JsonParser.object().from(httpResponse.body());
        } catch (JsonParserException e) {
            e.printStackTrace();
            return;
        }

        this.botPrefix = response.getString("prefix");
    }

    public void clearBot() {
        this.lastBotCommand = null;
        this.botPrefix = null;
    }

    @Nullable
    public String lastBotCommand() {
        if (this.lastBotCommand == null) return null;

        return "https://discord.com/channels/" + this.guildId + "/" + this.id + "/" + this.lastBotCommand;
    }

    @Nonnull
    public String id() {
        return this.id;
    }

    @Nullable
    public String botPrefix() {
        return this.botPrefix;
    }

    public void botPrefix(@Nonnull String newPrefix) {
        this.botPrefix = newPrefix;
    }

    @Nonnull
    public String botId() {
        return this.botId;
    }
}
