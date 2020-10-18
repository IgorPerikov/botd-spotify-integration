package com.github.igorperikov.botd;

import com.github.igorperikov.botd.domain.*;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BotdDataExtractor {
    private static final Logger log = LoggerFactory.getLogger(BotdDataExtractor.class);

    private final Sheets sheets;

    public BotdDataExtractor(Sheets sheets) {
        this.sheets = sheets;
    }

    public BotdData extract() {
        return new BotdData(parseBotdStages(), new BotdUsers(parseBotdUsers()));
    }

    private List<BotdStage> parseBotdStages() {
        List<BotdStage> result = new ArrayList<>();
        List<List<Object>> values = fetchBotdStagesData().getValues();

        BotdStage currentStage = null;
        for (int i = 0; i < values.size(); i++) {
            List<Object> row = values.get(i);
            Optional<BotdStage> nextStage = checkNextStage(row);
            if (nextStage.isPresent()) {
                if (currentStage != null) {
                    result.add(currentStage);
                }
                currentStage = nextStage.get();
            }
            if (currentStage == null) {
                throw new RuntimeException("Current botd stage remained null, incorrect data or parsing");
            }
            currentStage.getTracks().add(new BotdTrack(i, currentStage.getBand(), (String) row.get(2), getUser(row)));
        }

        return result;
    }

    private Optional<BotdStage> checkNextStage(List<Object> row) {
        String stageNumber = (String) row.get(0);
        if (StringUtils.isNotBlank(stageNumber)) {
            return Optional.of(new BotdStage(Integer.parseInt(stageNumber), getUser(row), (String) row.get(1)));
        }
        return Optional.empty();
    }

    private BotdUser getUser(List<Object> row) {
        return new BotdUser((String) row.get(3), (String) row.get(4));
    }

    private List<BotdUser> parseBotdUsers() {
        return fetchBotdUsersData().getValues()
                .stream()
                .filter(row -> !row.isEmpty())
                .map(row -> new BotdUser((String) row.get(0), (String) row.get(1)))
                .collect(Collectors.toList());
    }

    private ValueRange fetchBotdStagesData() {
        try {
            return sheets.spreadsheets()
                    .values()
                    .get(SpreadsheetsInit.SPREADSHEET_ID, "history!A2:E10000")
                    .execute();
        } catch (IOException e) {
            log.error("Unable to get botd history data", e);
            throw new RuntimeException(e);
        }
    }

    private ValueRange fetchBotdUsersData() {
        try {
            return sheets.spreadsheets()
                    .values()
                    .get(SpreadsheetsInit.SPREADSHEET_ID, "users!A1:B25")
                    .execute();
        } catch (IOException e) {
            log.error("Unable to get botd users data", e);
            throw new RuntimeException(e);
        }
    }
}
