package com.github.igorperikov.botd.accuracy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class DistanceCalculationUtils {
    private static final LevenshteinDistance LEVENSHTEIN_DISTANCE = new LevenshteinDistance();

    public static int minPossibleDistance(String first, String second) {
        return Math.min(
                Math.min(
                        distance(first, second),
                        distance(removeParenthesesContent(first), second)
                ),
                Math.min(
                        distance(first, removeParenthesesContent(second)),
                        distance(removeParenthesesContent(first), removeParenthesesContent(second))
                )
        );
    }

    public static int distance(String first, String second) {
        return LEVENSHTEIN_DISTANCE.apply(first.toLowerCase(), second.toLowerCase());
    }

    public static String removeParenthesesContent(String str) {
        if (str.contains("(") && str.contains(")")) {
            return StringUtils.normalizeSpace(str.replaceAll("(\\(.*?\\))", ""));
        } else {
            return str;
        }
    }
}
