package com.github.igorperikov.botd;

import com.github.igorperikov.botd.domain.BotdData;
import com.google.api.services.sheets.v4.Sheets;

public class Application {
    public static void main(String[] args) {
        Sheets sheets = SpreadsheetsInit.getService();
        BotdDataExtractor extractor = new BotdDataExtractor(sheets);

        BotdData botdData = extractor.extract();
    }
}
