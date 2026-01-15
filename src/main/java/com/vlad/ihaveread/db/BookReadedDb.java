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

    private static final String SELECT_READ_BOOKS_TBL_COLUMNS = """
            SELECT distinct b.id book_id, br.date_read,
             (select group_concat(a.name, '; ') from author a, author_book ab where ab.book_id = b.id and ab.author_id = a.id) authors,
             bn.name title,
             (NULLIF(bn.lib_file,'') is not null) has_lib_file,
             ifnull(NULLIF(bn.goodreads_id,''),
                (select 'alt:'||bn1.goodreads_id from book_names bn1 where bn1.book_id = b.id and NULLIF(bn1.goodreads_id,'') is not null limit 1)) goodreads_id,
             (select group_concat(t.name_uk, '; ') from tag t, book_tag bt where t.id = bt.tag_id and bt.book_id = b.id) tags,
             bn.lang lang_read, b.publish_date, br.medium, br.score, b.note""";

    Connection con;

    public BookReadedDb(Connection c) {
        this.con = c;
    }

    public List<BookReadedTblRow> getReadedBooksByCustomWhere(String wherePart) {
        String sql = SELECT_READ_BOOKS_TBL_COLUMNS + " " + """
            FROM book_readed br, book_names bn, book b, author_book ab, author a
            WHERE
            bn.book_id = b.id
            and br.book_name_id = bn.id
            and b.id = ab.book_id
            and ab.author_id = a.id
            and""" + " " + wherePart + " order by br.date_read";
        return getReadedBooksBySql(sql);
    }

    public List<BookReadedTblRow> getReadedBooksByYear(String dateRead) {
        String sql = SELECT_READ_BOOKS_TBL_COLUMNS + " " + """
            FROM book_readed br, book_names bn, book b, author_book ab, author a
            WHERE
            bn.book_id = b.id
            and br.book_name_id = bn.id
            and b.id = ab.book_id
            and ab.author_id = a.id
            and br.date_read like ?
            order by br.date_read""";
        return getReadedBooksBySql(sql, dateRead+"%");
    }

    public List<BookReadedTblRow> getReadedBooksByAuthor(String author) {
        String sql = SELECT_READ_BOOKS_TBL_COLUMNS + " " + """
            FROM book_readed br, book_names bn, book b, author_book ab, author a, author_names an
            WHERE
             bn.book_id = b.id
             and br.book_name_id = bn.id
             and b.id = ab.book_id
             and ab.author_id = a.id
             and an.author_id = a.id
             and an.name like ?
            order by br.date_read""";
        return getReadedBooksBySql(sql, "%"+author+"%");
    }

    public List<BookReadedTblRow> getReadedBooksByTitle(String title) {
        String sql = SELECT_READ_BOOKS_TBL_COLUMNS + " " + """
             FROM book_readed br, book_names bn, book b, author_book ab, author a
             WHERE
              bn.book_id = b.id
              and br.book_name_id = bn.id
              and b.id = ab.book_id
              and ab.author_id = a.id
              and bn.name like ?
             order by br.date_read""";
        return getReadedBooksBySql(sql, "%"+title+"%");
    }

    public List<BookReadedTblRow> getReadedBooksByTag(String tagNameEn) {
        String sql = SELECT_READ_BOOKS_TBL_COLUMNS + " " + """
            FROM book_readed br, book_names bn, book b, author_book ab, author a, tag t, book_tag bt
            WHERE
             bn.book_id = b.id
             and br.book_name_id = bn.id
             and b.id = ab.book_id
             and ab.author_id = a.id
             and bt.book_id = b.id
             and t.id = bt.tag_id
             and t.name_en = ?
            order by br.date_read""";
        return getReadedBooksBySql(sql, tagNameEn);
    }

    public List<BookReaded> getByBookId(int bookId) throws SQLException {
        List<BookReaded> ret = new ArrayList<>();
        String sql = "SELECT br.* FROM book_readed br, book_names bn WHERE br.book_name_id = bn.id and bn.book_id = ? ORDER BY br.date_read";
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

    public int getBookReadedCount() throws SQLException {
        int ret = 0;
        String sql = "SELECT count(*) FROM book_readed";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret = rs.getInt(1);
            }
            rs.close();
        }
        return ret;
    }

    private List<BookReadedTblRow> getReadedBooksBySql(String sql) {
        List<BookReadedTblRow> ret = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
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
            INSERT INTO book_readed (book_name_id, date_read, medium, score, note)
            VALUES (?, ?, ?, ?, ?) RETURNING id""";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, book.getBookNameId());
            ps.setString(2, book.getDateRead());
            ps.setString(3, book.getMedium());
            ps.setInt(4, book.getScore());
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

    public void updateBookReaded(BookReaded item) throws SQLException {
        String sql = """
            UPDATE book_readed
            SET book_name_id = ?, date_read = ?, medium = ?, score = ?, note = ?
            WHERE id = ?""";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, item.getBookNameId());
            ps.setString(2, item.getDateRead());
            ps.setString(3, item.getMedium());
            ps.setInt(4, item.getScore());
            ps.setString(5, item.getNote());
            ps.setInt(6, item.getId());
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
                .tags(rs.getString("tags"))
                .note(rs.getString("note"))
                .goodreadsId(composeGoodreadsId(rs))
                .hasFile(rs.getBoolean("has_lib_file")?"X":"")
                .build();
    }

    private String composeGoodreadsId(ResultSet rs) throws SQLException {
        String ret = rs.getString("goodreads_id");
        if (ret == null || ret.isEmpty()) {
            ret = "search:"+rs.getString("title")+" "+rs.getString("authors");
        }
        return ret;
    }

    public BookReaded getBookReadedFromRs(ResultSet rs) throws SQLException {
        return BookReaded.builder()
                .id(rs.getInt("id"))
                .bookNameId(rs.getInt("book_name_id"))
                .dateRead(rs.getString("date_read"))
                .medium(rs.getString("medium"))
                .score(rs.getInt("score"))
                .note(rs.getString("note"))
                .build();
    }
}
