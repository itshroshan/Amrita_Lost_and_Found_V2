package com.amrita.lostandfound.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TextMatchUtil {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for", "with", "is", "my", "lost", "found", "colour", "color", "class"
    ));

    // We now have a dedicated method to score just one piece of text against another
    public double calculateMatchPercentage(String text1, String text2) {
        if (text1 == null || text2 == null) return 0.0;

        Set<String> words1 = cleanAndTokenize(text1);
        Set<String> words2 = cleanAndTokenize(text2);

        if (words1.isEmpty() || words2.isEmpty()) return 0.0;

        int matchCount = 0;

        for (String w1 : words1) {
            for (String w2 : words2) {
                if (isSmartMatch(w1, w2)) {
                    matchCount++;
                    break;
                }
            }
        }

        int minSize = Math.min(words1.size(), words2.size());
        return ((double) matchCount / minSize) * 100.0;
    }

    // The new "Smart Match" logic to prevent 'pen' from matching 'pencil'
    private boolean isSmartMatch(String w1, String w2) {
        // 1. Exact match
        if (w1.equals(w2)) return true;

        // 2. Plurals (e.g., "keys" and "key")
        if (w1.equals(w2 + "s") || w2.equals(w1 + "s")) return true;

        // 3. Substrings (e.g., "mobile" and "mobilephone")
        // We ONLY allow this if the root word is at least 5 letters long!
        // This prevents "pen" from triggering "pencil", or "car" from triggering "carpet".
        if (w1.length() >= 5 && w2.contains(w1)) return true;
        if (w2.length() >= 5 && w1.contains(w2)) return true;

        return false;
    }

    private Set<String> cleanAndTokenize(String text) {
        return Arrays.stream(text.toLowerCase().replaceAll("[^a-z0-9\\s]", "").split("\\s+"))
                .filter(word -> !word.isEmpty() && !STOP_WORDS.contains(word))
                .collect(Collectors.toSet());
    }
}