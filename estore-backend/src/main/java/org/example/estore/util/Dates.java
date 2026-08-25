package org.example.estore.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Гибкий разбор дат/дат-со-временем из CSV-файлов */
public final class Dates {

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    };

    private static final DateTimeFormatter[] DATE_TIME_FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    };

    private Dates() {
    }

    /** @return null, если значение пустое */
    public static LocalDate parseDate(String value) {
        String v = normalize(value);
        if (v == null) {
            return null;
        }
        if (v.length() > 10 && v.contains("T")) {
            v = v.substring(0, 10);
        }
        RuntimeException last = null;
        for (DateTimeFormatter f : DATE_FORMATS) {
            try {
                return LocalDate.parse(v, f);
            } catch (RuntimeException e) {
                last = e;
            }
        }
        throw last != null ? last : new IllegalArgumentException("Некорректная дата: " + value);
    }

    /** @return null, если значение пустое */
    public static LocalDateTime parseDateTime(String value) {
        String v = normalize(value);
        if (v == null) {
            return null;
        }
        RuntimeException last = null;
        for (DateTimeFormatter f : DATE_TIME_FORMATS) {
            try {
                return LocalDateTime.parse(v, f);
            } catch (RuntimeException e) {
                last = e;
            }
        }
        LocalDate d = tryQuietly(() -> parseDate(v));
        if (d != null) {
            return d.atStartOfDay();
        }
        throw last != null ? last : new IllegalArgumentException("Некорректные дата/время: " + value);
    }

    private static LocalDate tryQuietly(java.util.function.Supplier<LocalDate> s) {
        try {
            return s.get();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }
}
