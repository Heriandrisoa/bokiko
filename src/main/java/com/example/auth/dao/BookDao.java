package com.example.auth.dao;

import com.example.auth.model.Book;
import com.example.auth.util.Db;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDao {
    private static final String COLS =
            "id, user_id, title, original_filename, file_path, cover_path, page_count, "
          + "file_size, sha256, last_page, last_read_at, created_at";

    /** Last read first, then most recently imported. */
    public List<Book> findByUser(long userId) throws SQLException {
        String sql = "SELECT " + COLS + " FROM books WHERE user_id = ? "
                   + "ORDER BY last_read_at DESC NULLS LAST, created_at DESC, id DESC";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Book> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    /** Ownership is part of the query: another user's id simply returns nothing. */
    public Optional<Book> findByIdAndUser(long id, long userId) throws SQLException {
        String sql = "SELECT " + COLS + " FROM books WHERE id = ? AND user_id = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public boolean existsByHash(long userId, String sha256) throws SQLException {
        String sql = "SELECT 1 FROM books WHERE user_id = ? AND sha256 = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, sha256);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public long insert(long userId, String title, String originalFilename, String filePath,
                       String coverPath, int pageCount, long fileSize, String sha256)
            throws SQLException {
        String sql = "INSERT INTO books (user_id, title, original_filename, file_path, cover_path, "
                   + "page_count, file_size, sha256) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, title);
            ps.setString(3, originalFilename);
            ps.setString(4, filePath);
            ps.setString(5, coverPath);
            ps.setInt(6, pageCount);
            ps.setLong(7, fileSize);
            ps.setString(8, sha256);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    /** Saves the reading position (clamped to 1..page_count) and bumps last_read_at. */
    public void updateProgress(long id, long userId, int page) throws SQLException {
        String sql = "UPDATE books SET last_page = LEAST(GREATEST(?, 1), page_count), "
                   + "last_read_at = now() WHERE id = ? AND user_id = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, page);
            ps.setLong(2, id);
            ps.setLong(3, userId);
            ps.executeUpdate();
        }
    }

    /** Marks the book as just opened. */
    public void touch(long id, long userId) throws SQLException {
        String sql = "UPDATE books SET last_read_at = now() WHERE id = ? AND user_id = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    public boolean delete(long id, long userId) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ? AND user_id = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private static Book map(ResultSet rs) throws SQLException {
        Timestamp lastRead = rs.getTimestamp("last_read_at");
        Timestamp created = rs.getTimestamp("created_at");
        return new Book(
                rs.getLong("id"), rs.getLong("user_id"), rs.getString("title"),
                rs.getString("original_filename"), rs.getString("file_path"),
                rs.getString("cover_path"), rs.getInt("page_count"), rs.getLong("file_size"),
                rs.getString("sha256"), rs.getInt("last_page"),
                lastRead == null ? null : lastRead.toInstant(),
                created == null ? Instant.now() : created.toInstant());
    }
}
