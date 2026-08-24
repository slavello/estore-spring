package org.example.estore.util;

import java.util.ArrayList;
import java.util.List;

/** Простой парсер CSV-строк с поддержкой кавычек и автоопределением разделителя */
public final class CsvParser {

    private CsvParser() {
    }

    /** Определение разделителя по заголовочной строке (вне кавычек) */
    public static char detectDelimiter(String headerLine) {
        long semicolons = countOutsideQuotes(headerLine, ';');
        long commas = countOutsideQuotes(headerLine, ',');
        long tabs = countOutsideQuotes(headerLine, '\t');
        if (tabs >= semicolons && tabs >= commas && tabs > 0) {
            return '\t';
        }
        return semicolons >= commas ? ';' : ',';
    }

    /** Разбор строки CSV в список полей */
    public static List<String> parseLine(String line, char delimiter) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean fieldStarted = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
                fieldStarted = true;
            } else if (c == delimiter) {
                fields.add(current.toString());
                current.setLength(0);
                fieldStarted = false;
            } else if (c != '\r' || i < line.length() - 1) {
                current.append(c);
                fieldStarted = true;
            }
        }
        fields.add(current.toString());
        // пустая строка без единого заполненного поля
        if (!fieldStarted && fields.size() == 1 && fields.get(0).isEmpty()) {
            return new ArrayList<>();
        }
        return fields;
    }

    private static long countOutsideQuotes(String line, char target) {
        long count = 0;
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == target && !inQuotes) {
                count++;
            }
        }
        return count;
    }
}
