package com.vlad.ihaveread.db;

import com.vlad.ihaveread.dao.Author;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthorDbTest {

    static SqliteDb sqliteDb = null;

    @BeforeAll
    static void setUpAll() throws SQLException {
        if (sqliteDb == null) {
            sqliteDb = new SqliteDb("jdbc:sqlite::memory:");
            sqliteDb.createTables();
        }
    }

    @Test
    @Order(1)
    void insertAuthor() throws SQLException {
        Author newAuthor = sqliteDb.getAuthorDb().insertAuthor(sqliteDb.getConnection(),
                "Test", "Author", "en", "note");
        assertNotNull(newAuthor);
        assertTrue(newAuthor.getId() > 0);
    }

    @Test
    @Order(2)
    void getByName() throws SQLException {
        String name = "Test, Author";
        Author author = sqliteDb.getAuthorDb().getByName(sqliteDb.getConnection(), name);
        assertNotNull(author);
        assertTrue(author.getId() > 0);
        assertEquals(author.getName(), name);
    }

    @Test
    @Order(3)
    void getAll() throws SQLException {
        List<Author> authors = sqliteDb.getAuthorDb().getAll(sqliteDb.getConnection());
        assertTrue(authors.size() > 0);
    }

    @Test
    @Order(4)
    void findByName() throws SQLException {
        String name = "Test, Author";
        String namePart = "%Author%";
        List<Author> authors = sqliteDb.getAuthorDb().findByName(sqliteDb.getConnection(), namePart);
        assertTrue(authors.size() > 0);
        assertEquals(authors.get(0).getName(), name);
    }


    @Test
    @Order(5)
    void deleteAuthor() throws SQLException {
        String name = "Test, Author";
        Author author = sqliteDb.getAuthorDb().getByName(sqliteDb.getConnection(), name);
        assertNotNull(author);
        sqliteDb.getAuthorDb().deleteAuthor(sqliteDb.getConnection(), author.getId());
        Author delAuthor = sqliteDb.getAuthorDb().getByName(sqliteDb.getConnection(), name);
        assertNull(delAuthor);
    }
}