package com.example.auth.model;

import java.time.Instant;

public class Book {
    private final long id;
    private final long userId;
    private final String title;
    private final String originalFilename;
    private final String filePath;      // relative to books.dir
    private final String coverPath;     // relative to books.dir, may be null
    private final int pageCount;
    private final long fileSize;
    private final String sha256;
    private final int lastPage;
    private final Instant lastReadAt;   // null = never opened
    private final Instant createdAt;

    public Book(long id, long userId, String title, String originalFilename, String filePath,
                String coverPath, int pageCount, long fileSize, String sha256,
                int lastPage, Instant lastReadAt, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.originalFilename = originalFilename;
        this.filePath = filePath;
        this.coverPath = coverPath;
        this.pageCount = pageCount;
        this.fileSize = fileSize;
        this.sha256 = sha256;
        this.lastPage = lastPage;
        this.lastReadAt = lastReadAt;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getOriginalFilename() { return originalFilename; }
    public String getFilePath() { return filePath; }
    public String getCoverPath() { return coverPath; }
    public int getPageCount() { return pageCount; }
    public long getFileSize() { return fileSize; }
    public String getSha256() { return sha256; }
    public int getLastPage() { return lastPage; }
    public Instant getLastReadAt() { return lastReadAt; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean isHasCover() { return coverPath != null && !coverPath.isBlank(); }

    public int getProgressPercent() {
        if (pageCount <= 0) return 0;
        return Math.min(100, (int) Math.round(lastPage * 100.0 / pageCount));
    }
}
