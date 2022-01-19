package org.neo4j.mapper.core.support;

import org.jetbrains.annotations.Nullable;

public class Assert {

    private Assert() {}

    public static void notNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasText(@Nullable String text, String message) {
        if (text == null || text.isEmpty() || text.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(@Nullable Boolean booleanValue, String message) {
        if (booleanValue == null || !booleanValue) {
            throw new IllegalArgumentException(message);
        }
    }


}
