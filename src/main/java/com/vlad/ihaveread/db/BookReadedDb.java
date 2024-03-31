package com.vlad.ihaveread.db;

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

    public List<BookReadedTblRow> getReadedBooksByYear(String dateRead) {
        String sql = """
        SELECT distinct br.book_id, br.date_read,
            (select group_concat(a.name, '; ') from author a, author_book ab where ab.book_id = b.id and ab.author_id = a.id) authors,
            ifnull((select bn.name from book_names bn where bn.book_id = b.id and bn.lang = br.lang_read), b.title) title,
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
                .build();
    }
}
