package com.example.auth.model;

import java.time.Instant;

public record BookMark(long id, long bookId, int page, String note, Instant createdAt) { }
