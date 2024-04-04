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

    public Book getById(int id) throws SQLException {
        Book ret = null;
        String sql = "SELECT * FROM book WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret = getBookFromRs(rs);
            }
            rs.close();
        }
        return ret;
    }

    public List<BookName> getBookNameByBookId(int bookId) throws SQLException {
        List<BookName> ret = new ArrayList<>();
        String sql = "SELECT * FROM book_names WHERE book_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(getBookNameFromRs(rs));
            }
            rs.close();
        }
        return ret;
    }

    public List<Book> findByName(String namePart) throws SQLException {
        List<Book> ret = new ArrayList<>();
        String sql = """
            SELECT distinct b.*
            FROM book b, book_names bn
            WHERE bn.name like ? and b.id = bn.book_id""";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, namePart);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(getBookFromRs(rs));
            }
            rs.close();
        }
        return ret;
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

    public void updateBook(Book book) throws SQLException {
        String sql = "UPDATE book SET title = ?, publish_date = ?, lang = ?, genre = ?, note = ?  WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getPublishDate());
            ps.setString(3, book.getLang());
            ps.setString(4, book.getGenre());
            ps.setString(5, book.getNote());
            ps.setInt(6, book.getId());
            int ret = ps.executeUpdate();
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

    public BookName insertBookName(BookName bookName) throws SQLException {
        String sql = "INSERT INTO book_names(book_id, name, lang) VALUES (?,?,?) RETURNING id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookName.getBookId());
            ps.setString(2, bookName.getName());
            ps.setString(3, bookName.getLang());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                bookName.setId(rs.getInt(1));
                return bookName;
            } else {
                return null;
            }
        }
    }

    public void updateBookName(BookName bookName) throws SQLException {
        String sql = "UPDATE book_names SET book_id = ?, name = ?, lang = ? WHERE id = ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookName.getBookId());
            ps.setString(2, bookName.getName());
            ps.setString(3, bookName.getLang());
            ps.setInt(4, bookName.getId());
            ps.executeUpdate();
        }
    }

    public void deleteBookName(int bookNameId) throws SQLException {
        String sql = "DELETE FROM book_names WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookNameId);
            ps.executeUpdate();
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

    public void insertBookAuthor(int bookId, int authorId) throws SQLException {
        String sql = "INSERT INTO author_book(book_id, author_id) VALUES (?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, authorId);
            ps.executeUpdate();
        }
    }

    public void deleteBookAuthor(int bookId, int authorId) throws SQLException {
        String sql = "DELETE FROM author_book WHERE book_id = ? AND author_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, authorId);
            ps.executeUpdate();
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
