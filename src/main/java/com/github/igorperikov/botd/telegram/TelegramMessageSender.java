package com.github.igorperikov.botd.telegram;

import org.apache.http.HttpHeaders;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Objects;

public class TelegramMessageSender {
    private static final String FLOW_API_BOTD_BOT_AUTHORIZATION = Objects.requireNonNull(
            System.getenv("FLOW_API_BOTD_BOT_AUTHORIZATION"),
            "provide FLOW_API_BOTD_BOT_AUTHORIZATION envvar"
    );
    private static final String TELEGRAM_CHAT_ID = Objects.requireNonNull(
            System.getenv("BOTD_TELEGRAM_CHAT_ID"),
            "provide BOTD_TELEGRAM_CHAT_ID envvar"
    );
    private static final String CONTENT_MANAGER_ID = Objects.requireNonNull(
            System.getenv("BOTD_TELEGRAM_CONTENT_MANAGER_ID"),
            "provide BOTD_TELEGRAM_CONTENT_MANAGER_ID envvar"
    );

    private static final String API_URI_STRING = "https://api.flow.ai/rest/v1/messages/";
    private static final String PAYLOAD_TEMPLATE = "{\"payload\": {\"type\": \"text\",\"speech\": \"%s\",\"originator\": {\"name\": \"botd-to-spotify-integration\",\"role\": \"user\"}}}";
    private static final String ROBOT_EMOJI = "\uD83E\uDD16";

    private final HttpClient httpClient;

    public TelegramMessageSender() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(1))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void sendAllChat(String message) {
        send(wrapWithRobotEmoji(message), TELEGRAM_CHAT_ID);
    }

    public void sendToContentManager(String message) {
        send(message, CONTENT_MANAGER_ID);
    }

    // TODO: retries
    private void send(String message, String recipientId) {
        try {
            httpClient.send(
                    HttpRequest.newBuilder()
                            .POST(HttpRequest.BodyPublishers.ofString(String.format(PAYLOAD_TEMPLATE, message)))
                            .uri(new URI(API_URI_STRING + URLEncoder.encode(recipientId, Charset.defaultCharset())))
                            .timeout(Duration.ofSeconds(2))
                            .header(HttpHeaders.CONTENT_TYPE, "application/json")
                            .header(HttpHeaders.AUTHORIZATION, FLOW_API_BOTD_BOT_AUTHORIZATION)
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static String wrapWithRobotEmoji(String message) {
        return ROBOT_EMOJI + message + ROBOT_EMOJI;
    }
}
