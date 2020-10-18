package com.github.igorperikov.botd;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsRequestInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;

public class SpreadsheetsInit {
    private static final Logger log = LoggerFactory.getLogger(SpreadsheetsInit.class);

    public static final String SPREADSHEET_ID = Objects.requireNonNull(
            System.getenv("BOTD_SPREADHSHEET_ID"),
            "provide BOTD_SPREADHSHEET_ID envvar"
    );
    private static final String TOKEN = Objects.requireNonNull(
            System.getenv("GOOGLE_SPREADSHEETS_API_KEY"),
            "provide GOOGLE_SPREADSHEETS_API_KEY envvar"
    );

    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static Sheets getService() {
        NetHttpTransport httpTransport;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to instantiate net http transport", e);
            throw new RuntimeException(e);
        }
        return new Sheets.Builder(httpTransport, JSON_FACTORY, null)
                .setGoogleClientRequestInitializer(new SheetsRequestInitializer(TOKEN))
                .setApplicationName("botd-spotify-integration")
                .build();
    }
}
