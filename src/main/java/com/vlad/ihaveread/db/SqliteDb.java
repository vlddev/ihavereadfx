package com.vlad.ihaveread.db;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

@Getter
public class SqliteDb implements AutoCloseable {

    private static Logger log = LoggerFactory.getLogger(SqliteDb.class);

    Connection connection;
    AuthorDb authorDb;
    BookDb bookDb;
    BookReadedDb bookReadedDb;

    public SqliteDb(String dbUrl) throws SQLException {
        this.connection = DriverManager.getConnection(dbUrl);
        this.authorDb = new AuthorDb(connection);
        this.bookDb = new BookDb(connection);
        this.bookReadedDb = new BookReadedDb(connection);
    }

    public void createTables() throws SQLException {
        Statement st = connection.createStatement();
        st.addBatch("""
                CREATE TABLE "author" (
                	"id"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                	"name"	TEXT NOT NULL UNIQUE,
                	"lang"	TEXT,
                	"note"	TEXT
                )""");
        st.addBatch("""
                CREATE TABLE "author_book" (
                    "author_id"	INTEGER NOT NULL,
                    "book_id"	INTEGER NOT NULL,
                    PRIMARY KEY("author_id","book_id")
                )""");
        st.addBatch("""
                CREATE TABLE "author_names" (
                	"author_id"	INTEGER NOT NULL,
                	"name"	TEXT NOT NULL,
                	"lang"	TEXT,
                	"type"	TEXT,
                	PRIMARY KEY("author_id","name")
                )""");
        st.addBatch("""
                CREATE TABLE "book" (
                	"id"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                	"title"	TEXT NOT NULL,
                	"publish_date"	TEXT,
                	"lang"	TEXT,
                	"genre"	TEXT,
                	"note"	TEXT
                )""");
        st.addBatch("""
                CREATE TABLE "book_names" (
                	"id"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                	"book_id"	INTEGER NOT NULL,
                	"name"	TEXT NOT NULL,
                	"lang"	TEXT NOT NULL
                )""");
        st.addBatch("""
                CREATE TABLE "book_readed" (
                	"id"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                	"book_id"	INTEGER NOT NULL,
                	"date_read"	TEXT,
                	"lang_read"	TEXT NOT NULL,
                	"medium"	TEXT,
                	"score"	INTEGER,
                	"title_read"	TEXT,
                	"note"	TEXT
                )""");
        st.executeBatch();
        st.close();
    }

    public void scanDb() {
        String[] tables = {"author","author_book","author_names","book","book_names","book_readed"};
        for (String table : tables) {
            try {
                long cnt = getRowCount(table);
                log.info("{} - {} row(s)", table, cnt);
            } catch (SQLException e) {
                log.warn("{} - {}", table, e.getMessage());
            }
        }
    }

    public long getRowCount(String tableName) throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("select count(*) from "+tableName);
        rs.next();
        long ret = rs.getLong(1);
        rs.close();
        st.close();
        return ret;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}
