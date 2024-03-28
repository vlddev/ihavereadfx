package com.vlad.ihaveread.db;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Getter
public class SqliteDb implements AutoCloseable {

    Connection connection;
    AuthorDb authorDb;

    public SqliteDb(String dbUrl) throws SQLException {
        this.connection = DriverManager.getConnection(dbUrl);
        this.authorDb = new AuthorDb();
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

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}
