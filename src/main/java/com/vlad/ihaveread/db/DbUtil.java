package com.vlad.ihaveread.db;

import com.vlad.ihaveread.dao.Book;
import com.vlad.ihaveread.dao.Tag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbUtil {

    SqliteDb sqliteDb;

    public DbUtil(SqliteDb sqliteDb) {
        this.sqliteDb = sqliteDb;
    }

    /*
    public void convertBookGenreToTags() throws SQLException {
        Connection con = sqliteDb.getConnection();
        String sql = "SELECT * FROM book ORDER BY id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Book book = sqliteDb.getBookDb().getBookFromRs(rs);
                if (book.getGenre() != null && book.getGenre().trim().length() > 0) {
                    List<String> unmatchedTags = new ArrayList<>();
                    List<Tag> matchedTags = new ArrayList<>();
                    String[] genreTags = book.getGenre().toLowerCase().split("[,;]");
                    for (String gTag : genreTags) {
                        gTag = gTag.trim();
                        List<Tag> foundTags = sqliteDb.getBookDb().findTagByName(gTag);
                        if (foundTags.size() == 1) {
                            matchedTags.add(foundTags.get(0));
                        } else {
                            unmatchedTags.add(gTag);
                        }
                    }
                    for (Tag tag : matchedTags) {
                        sqliteDb.getBookDb().insertBookTag(book.getId(), tag.getId());
                    }
                    book.setGenre(String.join("; ", unmatchedTags));
                    sqliteDb.getBookDb().updateBook(book);
                }
            }
            rs.close();
        }
    }
    */
}
