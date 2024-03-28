package com.vlad.ihaveread.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.vlad.ihaveread.dao.Author;
import com.vlad.ihaveread.dao.AuthorName;

public class AuthorDb {

    public Author getByName(Connection con, String name) throws SQLException {
        Author ret = null;
        String sql = "SELECT * FROM author WHERE name = ? COLLATE NOCASE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret = getFromRs(rs);
            }
            rs.close();
        }
        return ret;
    }

    public List<Author> getAll(Connection con) throws SQLException {
        List<Author> ret = new ArrayList<>();
        String sql = "SELECT * FROM author";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(getFromRs(rs));
            }
            rs.close();
        }
        return ret;
    }

    public List<Author> findByName(Connection con, String namePart) throws SQLException {
        List<Author> ret = new ArrayList<>();
        String sql = """
            SELECT distinct a.id, a.name, a.lang, a.note
            FROM author a, author_names an
            WHERE an.name like ? and a.id = an.author_id""";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, namePart);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(getFromRs(rs));
            }
            rs.close();
        }
        return ret;
    }

    public Author insertAuthor(Connection con, String surname, String names, String lang, String note) throws SQLException {
        String normName = surname.trim() + ", " + names.trim();
        String natName = names.trim() + " " + surname.trim();
        Author author = Author.builder().name(normName).lang(lang).note(note).build();
        author = insertAuthor(con, author);
        List<AuthorName> authorNames = new ArrayList<>(2);
        authorNames.add(AuthorName.builder().authorId(author.getId())
                .lang(lang).type(AuthorName.TYPE_NORM).name(normName)
                .build());
        authorNames.add(AuthorName.builder().authorId(author.getId())
                .lang(lang).type(AuthorName.TYPE_NATURAL).name(natName)
                .build());
        insertAuthorNames(con, authorNames);
        return author;
    }

    public Author insertAuthor(Connection con, Author author) throws SQLException {
        String sql = "INSERT INTO author(name, lang, note) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, author.getName());
            ps.setString(2, author.getLang());
            ps.setString(3, author.getNote());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                author.setId(rs.getInt(1));
                return author;
            } else {
                return null;
            }
        }
    }

    public void insertAuthorNames(Connection con, List<AuthorName> authorNames) throws SQLException {
        String sql = "INSERT INTO author_names(author_id, name, lang, type) VALUES (?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (AuthorName authorName : authorNames) {
                ps.setInt(1, authorName.getAuthorId());
                ps.setString(2, authorName.getName());
                ps.setString(3, authorName.getLang());
                ps.setString(4, authorName.getType());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void deleteAuthor(Connection con, int authorId) throws SQLException {
        String sql = "DELETE FROM author_book WHERE author_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            ps.executeUpdate();
        }
        sql = "DELETE FROM author_names WHERE author_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            ps.executeUpdate();
        }
        sql = "DELETE FROM author WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            ps.executeUpdate();
        }
    }

    public Author getFromRs(ResultSet rs) throws SQLException {
        return Author.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .lang(rs.getString("lang"))
                .note(rs.getString("note"))
                .build();
    }
}
