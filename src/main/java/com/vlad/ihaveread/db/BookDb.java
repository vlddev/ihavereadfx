package com.vlad.ihaveread.db;

import com.vlad.ihaveread.dao.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDb {
    Connection con;

    public BookDb(Connection c) {
        this.con = c;
    }

    public Book insertBook(Book book) throws SQLException {
        String sql = "INSERT INTO book(title, publish_date, lang, genre, note) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getPublishDate());
            ps.setString(3, book.getLang());
            ps.setString(4, book.getGenre());
            ps.setString(5, book.getNote());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                book.setId(rs.getInt(1));
                return book;
            } else {
                return null;
            }
        }
    }

    public void insertBookNames(List<BookName> bookNames) throws SQLException {
        String sql = "INSERT INTO book_names(book_id, name, lang) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (BookName bookName : bookNames) {
                ps.setInt(1, bookName.getBookId());
                ps.setString(2, bookName.getName());
                ps.setString(3, bookName.getLang());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void insertBookAuthors(Book book, List<Author> authors) throws SQLException {
        String sql = "INSERT INTO author_book(book_id, author_id) VALUES (?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Author author : authors) {
                ps.setInt(1, book.getId());
                ps.setInt(2, author.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Book getBookFromRs(ResultSet rs) throws SQLException {
        return Book.builder()
                .id(rs.getInt("id"))
                .title(rs.getString("title"))
                .lang(rs.getString("lang"))
                .publishDate(rs.getString("publish_date"))
                .genre(rs.getString("genre"))
                .note(rs.getString("note"))
                .build();
    }

    public BookName getBookNameFromRs(ResultSet rs) throws SQLException {
        return BookName.builder()
                .id(rs.getInt("id"))
                .bookId(rs.getInt("book_id"))
                .name(rs.getString("name"))
                .lang(rs.getString("lang"))
                .build();
    }
}
