package com.example.auth.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookTest {
    @Test
    void progressIsRoundedAndCapped() {
        assertEquals(67, book(2, 3, null).getProgressPercent());
        assertEquals(100, book(8, 3, null).getProgressPercent());
    }

    @Test
    void emptyBooksHaveNoProgressAndNoCover() {
        Book book = book(1, 0, null);
        assertEquals(0, book.getProgressPercent());
        assertFalse(book.isHasCover());
    }

    @Test
    void aNonBlankCoverIsRecognized() {
        assertTrue(book(1, 3, "covers/example.png").isHasCover());
    }

    private static Book book(int lastPage, int pageCount, String coverPath) {
        return new Book(1, 1, "Example", "example.pdf", "files/example.pdf", coverPath,
                pageCount, 1, "hash", lastPage, null, Instant.EPOCH);
    }
}
