package com.taller.service.support;

public final class TextSupport {

    private TextSupport() {
    }

    public static String normalizeOptional(String value) {
        return value == null ? null : value.trim();
    }

    public static String normalizeRequired(String value, String fallback) {
        String normalized = normalizeOptional(value);
        return normalized == null || normalized.isBlank() ? fallback : normalized;
    }
}
