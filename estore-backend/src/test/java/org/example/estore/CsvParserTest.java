package org.example.estore;

import org.example.estore.util.CsvParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvParserTest {

    @Test
    void detectsSemicolonDelimiter() {
        assertEquals(';', CsvParser.detectDelimiter("ФАМИЛИЯ;ИМЯ;ОТЧЕСТВО"));
        assertEquals(';', CsvParser.detectDelimiter("\"a;b\",x;y"));
    }

    @Test
    void detectsCommaDelimiter() {
        assertEquals(',', CsvParser.detectDelimiter("a,b,c"));
        assertEquals(',', CsvParser.detectDelimiter("id,\"name, full\",price"));
    }

    @Test
    void parsesQuotedFields() {
        List<String> fields = CsvParser.parseLine("\"Иванов; Иван\";25;;\"сказал \"\"привет\"\"\"", ';');
        assertEquals(4, fields.size());
        assertEquals("Иванов; Иван", fields.get(0));
        assertEquals("25", fields.get(1));
        assertTrue(fields.get(2).isEmpty());
        assertEquals("сказал \"привет\"", fields.get(3));
    }

    @Test
    void parsesEmptyLineAsNoFields() {
        List<String> fields = CsvParser.parseLine("", ';');
        assertTrue(fields.isEmpty());
    }
}
