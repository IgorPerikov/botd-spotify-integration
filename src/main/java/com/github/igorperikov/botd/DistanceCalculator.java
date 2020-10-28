package com.github.igorperikov.botd;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class DistanceCalculator {
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

    // TODO: extract to separate class?
    public static String removeParenthesesContent(String str) {
        if (str.contains("(") && str.contains(")")) {
            return StringUtils.normalizeSpace(str.replaceAll("(\\(.*?\\))", ""));
        } else {
            return str;
        }
    }
}
