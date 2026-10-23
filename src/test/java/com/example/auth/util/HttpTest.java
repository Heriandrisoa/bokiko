package com.example.auth.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpTest {
    @Test
    void parseIdAcceptsTrimmedPositiveValues() {
        assertEquals(42L, Http.parseId(" 42 "));
    }

    @Test
    void parseIdRejectsNonPositiveAndMalformedValues() {
        assertEquals(-1L, Http.parseId(null));
        assertEquals(-1L, Http.parseId("0"));
        assertEquals(-1L, Http.parseId("-9"));
        assertEquals(-1L, Http.parseId("book"));
    }

    @Test
    void escapeProducesSafeJsonStringContent() {
        assertEquals("quote\\\" slash\\\\ line\\n", Http.escape("quote\" slash\\ line\n"));
    }
}
