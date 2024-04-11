package com.vlad.ihaveread.db;

import com.vlad.ihaveread.dao.BookReaded;
import com.vlad.ihaveread.dao.BookReadedTblRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookReadedDb {
    Connection con;

    public BookReadedDb(Connection c) {
        this.con = c;
    }

    public List<BookReaded> getByBookId(int bookId) throws SQLException {
        List<BookReaded> ret = new ArrayList<>();
        String sql = "SELECT * FROM book_readed WHERE book_id = ? ORDER BY date_read";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(getBookReadedFromRs(rs));
            }
            rs.close();
        }
        return ret;
    }

    public List<BookReadedTblRow> getReadedBooksByYear(String dateRead) {
        String sql = """
        SELECT distinct br.book_id, br.date_read,
            (select group_concat(a.name, '; ') from author a, author_book ab where ab.book_id = b.id and ab.author_id = a.id) authors,
            ifnull((select bn.name from book_names bn where bn.book_id = b.id and bn.lang = br.lang_read), b.title) title,
            (select bn.goodreads_id from book_names bn where bn.book_id = b.id and bn.lang = br.lang_read) goodreads_id,
            br.lang_read, b.publish_date, br.medium, br.score, b.genre, b.note
        from book_readed br, book b, author_book ab, author a
        where
        br.book_id = b.id
        and br.book_id = ab.book_id
        and ab.author_id = a.id
        and br.date_read like ?
        order by br.date_read""";
        return getReadedBooksBySql(sql, dateRead+"%");
    }

    public List<BookReadedTblRow> getReadedBooksByAuthor(String author) {
        String sql = """
            SELECT distinct br.book_id, br.date_read,
                 (select group_concat(a.name, '; ') from author a, author_book ab where ab.book_id = b.id and ab.author_id = a.id) authors,
                 ifnull((select bn.name from book_names bn where bn.book_id = b.id and bn.lang = br.lang_read), b.title) title,
                 (select bn.goodreads_id from book_names bn where bn.book_id = b.id and bn.lang = br.lang_read) goodreads_id,
                 br.lang_read, b.publish_date, br.medium, br.score, b.genre, b.note
            FROM book_readed br, book b, author_book ab, author a, author_names an
            WHERE
             br.book_id = b.id
             and br.book_id = ab.book_id
             and ab.author_id = a.id
             and an.author_id = a.id
             and an.name like ?
            order by br.date_read""";
        return getReadedBooksBySql(sql, "%"+author+"%");
    }

    public List<BookReadedTblRow> getReadedBooksByTitle(String title) {
        String sql = """
            SELECT distinct br.book_id, br.date_read,
                 (select group_concat(a.name, '; ') from author a, author_book ab where ab.book_id = b.id and ab.author_id = a.id) authors,
                 ifnull((select bn.name from book_names bn where bn.book_id = b.id and bn.lang = br.lang_read), b.title) title,
                 (select bn.goodreads_id from book_names bn where bn.book_id = b.id and bn.lang = br.lang_read) goodreads_id,
                 br.lang_read, b.publish_date, br.medium, br.score, b.genre, b.note
             from book_readed br, book b, author_book ab, author a
             where
             br.book_id = b.id
             and br.book_id = ab.book_id
             and ab.author_id = a.id
             and b.id in (select distinct book_id from book_names where name like ?)
             order by br.date_read""";
        return getReadedBooksBySql(sql, "%"+title+"%");
    }

    private List<BookReadedTblRow> getReadedBooksBySql(String sql, String param) {
        List<BookReadedTblRow> ret = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(getFromRs(rs));
            }
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }

    public BookReaded insertBookReaded(BookReaded book) throws SQLException {
        String sql = """
            INSERT INTO book_readed (book_id, date_read, lang_read, medium, score, note)
            VALUES (?, ?, ?, ?, ?, ?) RETURNING id""";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, book.getBookId());
            ps.setString(2, book.getDateRead());
            ps.setString(3, book.getLangRead());
            ps.setString(4, book.getMedium());
            ps.setInt(5, book.getScore());
            ps.setString(6, book.getNote());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                book.setId(rs.getInt(1));
                return book;
            } else {
                return null;
            }
        }
    }

    public void updateBookReaded(BookReaded item) throws SQLException {
        String sql = """
            UPDATE book_readed
            SET book_id = ?, date_read = ?, lang_read = ?, medium = ?, score = ?, note = ?
            WHERE id = ?""";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, item.getBookId());
            ps.setString(2, item.getDateRead());
            ps.setString(3, item.getLangRead());
            ps.setString(4, item.getMedium());
            ps.setInt(5, item.getScore());
            ps.setString(6, item.getNote());
            ps.setInt(7, item.getId());
            ps.executeUpdate();
        }
    }

    public void deleteBookReaded(int bookReadedId) throws SQLException {
        String sql = "DELETE FROM book_readed WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookReadedId);
            ps.executeUpdate();
        }
    }

    public BookReadedTblRow getFromRs(ResultSet rs) throws SQLException {
        return BookReadedTblRow.builder()
                .bookId(rs.getInt("book_id"))
                .dateRead(rs.getString("date_read"))
                .authors(rs.getString("authors"))
                .titleRead(rs.getString("title"))
                .langRead(rs.getString("lang_read"))
                .publishDate(rs.getString("publish_date"))
                .medium(rs.getString("medium"))
                .score(rs.getInt("score"))
                .genre(rs.getString("genre"))
                .note(rs.getString("note"))
                .goodreadsId(composeGoodreadsId(rs))
                .build();
    }

    private String composeGoodreadsId(ResultSet rs) throws SQLException {
        String ret = rs.getString("goodreads_id");
        if (ret == null || ret.length() == 0) {
            ret = "search:"+rs.getString("title")+" "+rs.getString("authors");
        }
        return ret;
    }

    public BookReaded getBookReadedFromRs(ResultSet rs) throws SQLException {
        return BookReaded.builder()
                .id(rs.getInt("id"))
                .bookId(rs.getInt("book_id"))
                .dateRead(rs.getString("date_read"))
                .langRead(rs.getString("lang_read"))
                .medium(rs.getString("medium"))
                .score(rs.getInt("score"))
                .note(rs.getString("note"))
                .build();
    }
}
