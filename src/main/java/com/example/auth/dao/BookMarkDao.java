package com.example.auth.dao;

import com.example.auth.model.BookMark;
import com.example.auth.util.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BookMarkDao {
    public List<BookMark> findByBook(long bookId, long userId) throws SQLException {
        String sql = "SELECT id, book_id, page, note, created_at FROM book_marks "
                + "WHERE book_id = ? AND user_id = ? ORDER BY created_at DESC";
        try (Connection connection = Db.get(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookId);
            statement.setLong(2, userId);
            try (ResultSet result = statement.executeQuery()) {
                List<BookMark> marks = new ArrayList<>();
                while (result.next()) marks.add(map(result));
                return marks;
            }
        }
    }

    public BookMark insert(long bookId, long userId, int page, String note) throws SQLException {
        String sql = "INSERT INTO book_marks (book_id, user_id, page, note) VALUES (?, ?, ?, ?) "
                + "RETURNING id, book_id, page, note, created_at";
        try (Connection connection = Db.get(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookId);
            statement.setLong(2, userId);
            statement.setInt(3, page);
            statement.setString(4, note);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return map(result);
            }
        }
    }

    public boolean delete(long id, long bookId, long userId) throws SQLException {
        String sql = "DELETE FROM book_marks WHERE id = ? AND book_id = ? AND user_id = ?";
        try (Connection connection = Db.get(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.setLong(2, bookId);
            statement.setLong(3, userId);
            return statement.executeUpdate() > 0;
        }
    }

    private static BookMark map(ResultSet result) throws SQLException {
        Timestamp createdAt = result.getTimestamp("created_at");
        return new BookMark(result.getLong("id"), result.getLong("book_id"), result.getInt("page"),
                result.getString("note"), createdAt == null ? Instant.now() : createdAt.toInstant());
    }
}
