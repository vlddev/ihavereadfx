package com.vlad.ihaveread.db;

import com.vlad.ihaveread.dao.*;

import java.nio.file.Path;
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

    public int getBookCount() throws SQLException {
        int ret = 0;
        String sql = "SELECT count(*) FROM book";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret = rs.getInt(1);
            }
            rs.close();
        }
        return ret;
    }

    public List<BookName> getBookNameByBookId(int bookId, Author author) throws SQLException {
        List<BookName> ret = new ArrayList<>();
        String sql = "SELECT * FROM book_names WHERE book_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BookName bn = getBookNameFromRs(rs);
                bn.setBookLibFile(composeBookLibFile(bn, author));
                ret.add(bn);
            }
            rs.close();
        }
        return ret;
    }

    public List<Tag> getBookTagsByBookId(int bookId) throws SQLException {
        List<Tag> ret = new ArrayList<>();
        String sql = """
            SELECT t.*
            FROM tag t, book_tag bt
            WHERE bt.book_id = ? and t.id = bt.tag_id""";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(getTagFromRs(rs));
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

    public List<Tag> findTagLikeName(String namePart) throws SQLException {
        List<Tag> ret = new ArrayList<>();
        String sql = """
            SELECT t.*
            FROM tag t
            WHERE t.name_en like ? or t.name_uk like ?
            order by t.name_en""";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, namePart);
            ps.setString(2, namePart);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(getTagFromRs(rs));
            }
            rs.close();
        }
        return ret;
    }

    public List<Tag> findTagByName(String name) throws SQLException {
        List<Tag> ret = new ArrayList<>();
        String sql = """
            SELECT t.*
            FROM tag t
            WHERE t.name_en = ? or t.name_uk = ?""";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, name);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(getTagFromRs(rs));
            }
            rs.close();
        }
        return ret;
    }

    public Tag insertTag(Tag tag) throws SQLException {
        String sql = "INSERT INTO tag(name_en, name_uk) VALUES (?, ?) RETURNING id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tag.getNameEn());
            ps.setString(2, tag.getNameUk());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tag.setId(rs.getInt(1));
                return tag;
            } else {
                return null;
            }
        }
    }

    public void updateTag(Tag tag) throws SQLException {
        String sql = "UPDATE tag SET name_en = ?, name_uk = ?  WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tag.getNameEn());
            ps.setString(2, tag.getNameUk());
            ps.setInt(3, tag.getId());
            int ret = ps.executeUpdate();
        }
    }

    public Book insertBook(Book book) throws SQLException {
        String sql = "INSERT INTO book(title, publish_date, lang, series, note) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getPublishDate());
            ps.setString(3, book.getLang());
            ps.setString(4, book.getSeries());
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
        String sql = "UPDATE book SET title = ?, publish_date = ?, lang = ?, series = ?, note = ?  WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getPublishDate());
            ps.setString(3, book.getLang());
            ps.setString(4, book.getSeries());
            ps.setString(5, book.getNote());
            ps.setInt(6, book.getId());
            int ret = ps.executeUpdate();
        }
    }

    public void deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM author_book WHERE book_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
        }
        sql = "DELETE FROM book_names WHERE book_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
        }
        sql = "DELETE FROM book_readed WHERE book_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
        }
        sql = "DELETE FROM book WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
        }
    }

    public void insertBookNames(List<BookName> bookNames) throws SQLException {
        String sql = "INSERT INTO book_names(book_id, name, lang, goodreads_id, lib_file) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (BookName bookName : bookNames) {
                ps.setInt(1, bookName.getBookId());
                ps.setString(2, bookName.getName());
                ps.setString(3, bookName.getLang());
                ps.setString(4, bookName.getGoodreadsId());
                ps.setString(5, bookName.getLibFile());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public BookName insertBookName(BookName bookName) throws SQLException {
        String sql = "INSERT INTO book_names(book_id, name, lang, goodreads_id, lib_file) VALUES (?,?,?,?,?) RETURNING id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookName.getBookId());
            ps.setString(2, bookName.getName());
            ps.setString(3, bookName.getLang());
            ps.setString(4, bookName.getGoodreadsId());
            ps.setString(5, bookName.getLibFile());
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
        String sql = """
            UPDATE book_names
            SET book_id = ?, name = ?, lang = ?, goodreads_id = ?, lib_file = ?
            WHERE id = ?""";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookName.getBookId());
            ps.setString(2, bookName.getName());
            ps.setString(3, bookName.getLang());
            ps.setString(4, bookName.getGoodreadsId());
            ps.setString(5, bookName.getLibFile());
            ps.setInt(6, bookName.getId());
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

    public void insertBookTag(int bookId, int tagId) throws SQLException {
        String sql = "INSERT INTO book_tag(book_id, tag_id) VALUES (?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, tagId);
            ps.executeUpdate();
        }
    }

    public void deleteBookTag(int bookId, int tagId) throws SQLException {
        String sql = "DELETE FROM book_tag WHERE book_id = ? AND tag_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, tagId);
            ps.executeUpdate();
        }
    }

    public Book getBookFromRs(ResultSet rs) throws SQLException {
        return Book.builder()
                .id(rs.getInt("id"))
                .title(rs.getString("title"))
                .lang(rs.getString("lang"))
                .publishDate(rs.getString("publish_date"))
                .series(rs.getString("series"))
                .note(rs.getString("note"))
                .build();
    }

    public BookName getBookNameFromRs(ResultSet rs) throws SQLException {
        return BookName.builder()
                .id(rs.getInt("id"))
                .bookId(rs.getInt("book_id"))
                .name(rs.getString("name"))
                .lang(rs.getString("lang"))
                .goodreadsId(rs.getString("goodreads_id"))
                .libFile(rs.getString("lib_file"))
                .build();
    }

    public Tag getTagFromRs(ResultSet rs) throws SQLException {
        return Tag.builder()
                .id(rs.getInt("id"))
                .nameEn(rs.getString("name_en"))
                .nameUk(rs.getString("name_uk"))
                .build();
    }

    public BookLibFile composeBookLibFile(BookName bn, Author author) {
        BookLibFile ret = BookLibFile.builder()
                .libFile(bn.getLibFile())
                .bookNameId(bn.getId())
                .bookName(bn.getName())
                .build();
        if (bn.getLibFile() == null || bn.getLibFile().length() == 0) {
            ret.setBookDir(author.getBaseDir(bn));
        } else {
            ret.setBookDir(Path.of(bn.getLibFile()).getParent().toString());
        }
        return ret;
    }

}
