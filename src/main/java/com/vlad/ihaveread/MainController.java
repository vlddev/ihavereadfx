package com.vlad.ihaveread;

import com.vlad.ihaveread.dao.*;
import com.vlad.ihaveread.db.SqliteDb;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class MainController {

    private static Logger log = LoggerFactory.getLogger(MainController.class);

    private SqliteDb sqliteDb;
    private Author curAuthor;
    private Book curBook;
    private AuthorName curAuthorName;

    private NewAuthorDialog newAuthorDialog;
    private NewBookreadedDialog newBookreadedDialog;

    @FXML
    private TextField tfSearchText, tfAuthorName, tfAuthorLang, tfAuthorNote;

    @FXML
    private ListView lstFoundAuthors, lstAuthorNames, lstFoundBooks, lstBookAuthors, lstBookNames, lstReadBooks;

    @FXML
    private TextField tfAuthorNamesName, tfAuthorNamesLang, tfAuthorNamesType;

    @FXML
    private Tab tabBook;
    @FXML
    private TabPane tabPane;

    @FXML
    private TextField tfSearchReadedText;
    @FXML
    private TableView tvReadedBooks;

    @FXML
    private TextField tfBookSearchText, tfBookTitle, tfBookLang, tfPublishDate, tfGenre;

    @FXML
    private TextArea taBookNote;

    public void setSqliteDb(SqliteDb sqliteDb) {
        this.sqliteDb = sqliteDb;
    }

    public void setNewAuthorDialog(NewAuthorDialog newAuthorDialog) {
        this.newAuthorDialog = newAuthorDialog;
    }

    public void setNewBookreadedDialog(NewBookreadedDialog newBookreadedDialog) {
        this.newBookreadedDialog = newBookreadedDialog;
    }

    public void initListeners() {
        lstFoundAuthors.getSelectionModel().selectedItemProperty().addListener(
                (ChangeListener<Author>) (ov, oldVal, newVal) -> onSelectAuthor(newVal));
        lstFoundAuthors.setCellFactory(callback -> new ListCell<Author>() {
            @Override
            protected void updateItem(Author item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        lstAuthorNames.getSelectionModel().selectedItemProperty().addListener(
                (ChangeListener<AuthorName>) (ov, oldVal, newVal) -> onSelectAuthorName(newVal));
        lstAuthorNames.setCellFactory(callback -> new ListCell<AuthorName>() {
            @Override
            protected void updateItem(AuthorName item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        lstFoundBooks.getSelectionModel().selectedItemProperty().addListener(
                (ChangeListener<Book>) (ov, oldVal, newVal) -> onSelectBookName(newVal));
        lstFoundBooks.setCellFactory(callback -> new ListCell<Book>() {
            @Override
            protected void updateItem(Book item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                }
            }
        });
        lstBookAuthors.setCellFactory(callback -> new ListCell<Author>() {
            @Override
            protected void updateItem(Author item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        lstBookNames.setCellFactory(callback -> new ListCell<BookName>() {
            @Override
            protected void updateItem(BookName item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getLang()+" | "+item.getName());
                }
            }
        });
        tvReadedBooks.setRowFactory(tv -> {
            TableRow<BookReadedTblRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    BookReadedTblRow rowData = row.getItem();
                    showInBookTab(rowData);
                }
            });
            return row ;
        });
    }

    public void initComponents(Scene scene) throws IOException {
        setNewAuthorDialog(new NewAuthorDialog(scene.getWindow(), sqliteDb));
        setNewBookreadedDialog(new NewBookreadedDialog(scene.getWindow(), sqliteDb));
    }

    public void doSearchAuthor(ActionEvent actionEvent) throws SQLException {
        clearAuthor();
        String strToFind = tfSearchText.getText().trim();
        log.info("Search for author '{}'", strToFind);
        if (strToFind.length() > 0) {
            List<Author> authors = sqliteDb.getAuthorDb().findByName("%"+strToFind+"%");
            lstFoundAuthors.getItems().clear();
            if (!authors.isEmpty()) {
                lstFoundAuthors.getItems().addAll(authors);
            } else {
                log.info("Nothing found");
            }
        }
    }

    public void clearAuthor() {
        tfAuthorName.clear();
        tfAuthorLang.clear();
        tfAuthorNote.clear();
        curAuthor = null;
        lstAuthorNames.getItems().clear();
    }

    public void onSelectAuthor(Author author) {
        if (author == null) {
            clearAuthor();
            return;
        }
        tfAuthorName.setText(author.getName());
        tfAuthorLang.setText(author.getLang());
        tfAuthorNote.setText(author.getNote());
        curAuthor = author;
        loadAuthorNames(author.getId());
    }

    public void loadAuthorNames(int authorId) {
        try {
            List<AuthorName> authorNames = sqliteDb.getAuthorDb().getAuthorNames(authorId);
            clearAuthorName();
            lstAuthorNames.getItems().clear();
            lstAuthorNames.getItems().addAll(authorNames);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearAuthorName() {
        tfAuthorNamesName.clear();
        tfAuthorNamesLang.clear();
        tfAuthorNamesType.clear();
        curAuthorName = null;
    }

    public void onSelectAuthorName(AuthorName authorName) {
        if (authorName == null) {
            clearAuthorName();
            return;
        }
        tfAuthorNamesName.setText(authorName.getName());
        tfAuthorNamesLang.setText(authorName.getLang());
        tfAuthorNamesType.setText(authorName.getType());
        curAuthorName = authorName;
    }

    public void doAddAuthor(ActionEvent actionEvent) {
        newAuthorDialog.showAndWait();
    }

    public void doSaveAuthor(ActionEvent actionEvent) {
        if (curAuthor != null) {
            curAuthor.setName(tfAuthorName.getText());
            curAuthor.setLang(tfAuthorLang.getText());
            curAuthor.setNote(tfAuthorNote.getText());
            try {
                sqliteDb.getAuthorDb().updateAuthor(curAuthor);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void doDeleteAuthor(ActionEvent actionEvent) {
        if (curAuthor != null) {
            try {
                sqliteDb.getAuthorDb().deleteAuthor(curAuthor.getId());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void doSaveAuthorName(ActionEvent actionEvent) {
        if (curAuthorName != null) {
            String oldName = curAuthorName.getName();
            curAuthorName.setName(tfAuthorNamesName.getText());
            curAuthorName.setLang(tfAuthorNamesLang.getText());
            curAuthorName.setType(tfAuthorNamesType.getText());
            try {
                sqliteDb.getAuthorDb().updateAuthorName(oldName, curAuthorName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            loadAuthorNames(curAuthorName.getAuthorId());
        }
    }

    public void doAddAuthorName(ActionEvent actionEvent) {
        String strAuthorName = tfAuthorNamesName.getText().trim();
        if (curAuthor != null && curAuthorName == null && strAuthorName.length() > 0) {
            List<AuthorName> authorNames = new ArrayList<>(1);
            authorNames.add(AuthorName.builder().authorId(curAuthor.getId())
                    .lang(tfAuthorNamesLang.getText()).type(tfAuthorNamesType.getText()).name(strAuthorName)
                    .build());
            try {
                sqliteDb.getAuthorDb().insertAuthorNames(authorNames);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            loadAuthorNames(curAuthor.getId());
        }
    }

    public void doDeleteAuthorName(ActionEvent actionEvent) {
        if (curAuthorName != null) {
            try {
                sqliteDb.getAuthorDb().deleteAuthorName(curAuthorName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            loadAuthorNames(curAuthor.getId());
        }
    }

    public void doSearchReadedByTitle(ActionEvent actionEvent) {
        doSearchReadedBy(sqliteDb.getBookReadedDb()::getReadedBooksByTitle);
    }

    public void doSearchReadedByAuthor(ActionEvent actionEvent) {
        doSearchReadedBy(sqliteDb.getBookReadedDb()::getReadedBooksByAuthor);
    }

    public void doSearchReadedByYear(ActionEvent actionEvent) {
        doSearchReadedBy(sqliteDb.getBookReadedDb()::getReadedBooksByYear);
    }

    private void doSearchReadedBy(Function<String, List<BookReadedTblRow>> getBy) {
        String strToFind = tfSearchReadedText.getText().trim();
        log.info("Search by year '{}'", strToFind);
        if (strToFind.length() > 0) {
            List<BookReadedTblRow> books = getBy.apply(strToFind);
            tvReadedBooks.getItems().clear();
            if (!books.isEmpty()) {
                tvReadedBooks.getItems().addAll(books);
            } else {
                log.info("Nothing found");
            }
        }
    }

    public void clearBook() {
        tfBookTitle.clear();
        tfBookLang.clear();
        tfPublishDate.clear();
        tfGenre.clear();
        taBookNote.clear();
        curBook = null;
        lstBookAuthors.getItems().clear();
        lstBookNames.getItems().clear();
        lstReadBooks.getItems().clear();
    }

    public void doSearchBook(ActionEvent actionEvent) throws SQLException {
        clearBook();
        String strToFind = tfBookSearchText.getText().trim();
        log.info("Search for book '{}'", strToFind);
        if (strToFind.length() > 0) {
            List<Book> books = sqliteDb.getBookDb().findByName("%"+strToFind+"%");
            lstFoundBooks.getItems().clear();
            if (!books.isEmpty()) {
                lstFoundBooks.getItems().addAll(books);
            } else {
                log.info("Nothing found");
            }
        }
    }

    public void showInBookTab(BookReadedTblRow bookReaded) {
        try {
            Book book = sqliteDb.getBookDb().getById(bookReaded.getBookId());
            lstFoundBooks.getItems().clear();
            lstFoundBooks.getItems().add(book);
            lstFoundBooks.getSelectionModel().select(book);
            tabPane.getSelectionModel().select(tabBook);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void onSelectBookName(Book book) {
        clearBook();
        if (book == null) {
            return;
        }
        curBook = book;
        tfBookTitle.setText(book.getTitle());
        tfBookLang.setText(book.getLang());
        tfPublishDate.setText(book.getPublishDate());
        tfGenre.setText(book.getGenre());
        taBookNote.setText(book.getNote());
        curBook = null;
        // fill lists
        try {
            lstBookAuthors.getItems().addAll(sqliteDb.getAuthorDb().getByBookId(book.getId()));
            lstBookNames.getItems().addAll(sqliteDb.getBookDb().getBookNameByBookId(book.getId()));
            lstReadBooks.getItems().addAll(sqliteDb.getBookReadedDb().getByBookId(book.getId()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void doAddBook(ActionEvent actionEvent) {
        newBookreadedDialog.showAndWait();
    }
}