package com.animegen.service;

import com.animegen.common.BizException;
import com.animegen.common.ErrorCodes;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class SensitiveWordFilter {
    private static final List<String> WORDS = List.of(
            "sb",
            "cao",
            "fuck",
            "nazi",
            "porn",
            "drug"
    );

    public void validate(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String word : WORDS) {
            if (lower.contains(word.toLowerCase(Locale.ROOT))) {
                throw new BizException(ErrorCodes.SENSITIVE_WORD_HIT, "text contains sensitive word");
            }
        }
    }
}
