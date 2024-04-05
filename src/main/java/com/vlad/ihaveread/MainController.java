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
import java.util.Optional;
import java.util.function.Function;

import javafx.scene.input.MouseButton;
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
    private SelectAuthorDialog selectAuthorDialog;
    private EditBookNameDialog editBookNameDialog;
    private EditBookReadedDialog editBookReadedDialog;

    @FXML
    private TextField tfSearchText, tfAuthorName, tfAuthorLang, tfAuthorNote;

    @FXML
    private ListView lstFoundAuthors, lstAuthorNames, lstFoundBooks, lstBookAuthors, lstBookNames;

    @FXML
    private TextField tfAuthorNamesName, tfAuthorNamesLang, tfAuthorNamesType;

    @FXML
    private Tab tabBook;
    @FXML
    private TabPane tabPane;

    @FXML
    private TextField tfSearchReadedText;
    @FXML
    private TableView tvReadedBooks, lstReadBooks;

    @FXML
    private TextField tfBookSearchText, tfBookTitle, tfBookLang, tfPublishDate, tfGenre;

    @FXML
    private TextArea taBookNote;

    public void setSqliteDb(SqliteDb sqliteDb) {
        this.sqliteDb = sqliteDb;
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
                    setOnMouseClicked(mouseClickedEvent -> {
                        if (mouseClickedEvent.getButton().equals(MouseButton.PRIMARY) && mouseClickedEvent.getClickCount() == 2) {
                            doEditBookName(null);
                        }
                    });                }
            }
        });
        // double-click on table row
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
        // double-click on table row
        lstReadBooks.setRowFactory(tv -> {
            TableRow<BookReaded> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    BookReaded rowData = row.getItem();
                    doEditReadBook(null);
                }
            });
            return row ;
        });
    }

    public void initComponents(Scene scene) throws IOException {
        newAuthorDialog = new NewAuthorDialog(scene.getWindow(), sqliteDb);
        newBookreadedDialog = new NewBookreadedDialog(scene.getWindow(), sqliteDb);
        selectAuthorDialog = new SelectAuthorDialog(scene.getWindow(), sqliteDb);
        editBookNameDialog = new EditBookNameDialog(scene.getWindow());
        editBookReadedDialog = new EditBookReadedDialog(scene.getWindow());
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
            // TODO ask "are you sure?"
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
        log.info("Search for '{}'", strToFind);
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

    public void doAddBookAuthor(ActionEvent actionEvent) throws SQLException {
        Optional<Author> ret = selectAuthorDialog.showAndWait();
        if (ret.isPresent()) {
            if (!lstBookAuthors.getItems().contains(ret.get())) {
                sqliteDb.getBookDb().insertBookAuthor(curBook.getId(), ret.get().getId());
                lstBookAuthors.getItems().add(ret.get());
            }
        }
    }

    public void doDeleteBookAuthor(ActionEvent actionEvent) throws SQLException {
        int selInd = lstBookAuthors.getSelectionModel().getSelectedIndex();
        Author author = (Author)lstBookAuthors.getItems().get(selInd);
        sqliteDb.getBookDb().deleteBookAuthor(curBook.getId(), author.getId());
        lstBookAuthors.getItems().remove(selInd);
    }

    public void doSaveBook(ActionEvent actionEvent) throws SQLException {
        curBook.setLang(tfBookLang.getText().trim());
        curBook.setTitle(tfBookTitle.getText().trim());
        curBook.setGenre(tfGenre.getText().trim());
        curBook.setPublishDate(tfPublishDate.getText().trim());
        curBook.setNote(taBookNote.getText().trim());
        sqliteDb.getBookDb().updateBook(curBook);
    }

    public void doAddBookName(ActionEvent actionEvent) throws SQLException {
        editBookNameDialog.setBookName(null);
        Optional<BookName> ret = editBookNameDialog.showAndWait();
        if (ret.isPresent()) {
            ret.get().setBookId(curBook.getId());
            BookName newEntity = sqliteDb.getBookDb().insertBookName(ret.get());
            lstBookNames.getItems().add(newEntity);
        }
    }

    public void doEditBookName(ActionEvent actionEvent) {
        int selInd = lstBookNames.getSelectionModel().getSelectedIndex();
        if (selInd > -1) {
            editBookNameDialog.setBookName((BookName)lstBookNames.getItems().get(selInd));
            Optional<BookName> ret = editBookNameDialog.showAndWait();
            if (ret.isPresent()) {
                try {
                    sqliteDb.getBookDb().updateBookName(ret.get());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                lstBookNames.getItems().set(selInd, ret.get());
            }
        }
    }

    public void doDeleteBookName(ActionEvent actionEvent) throws SQLException {
        int selInd = lstBookNames.getSelectionModel().getSelectedIndex();
        if (selInd > -1) {
            BookName item = (BookName)lstBookNames.getItems().get(selInd);
            sqliteDb.getBookDb().deleteBookName(item.getId());
            lstBookNames.getItems().remove(selInd);
        }
    }

    public void doAddReadBook(ActionEvent actionEvent) throws SQLException {
        editBookReadedDialog.setEntity(null);
        Optional<BookReaded> ret = editBookReadedDialog.showAndWait();
        if (ret.isPresent()) {
            ret.get().setBookId(curBook.getId());
            BookReaded newEntity = sqliteDb.getBookReadedDb().insertBookReaded(ret.get());
            lstReadBooks.getItems().add(newEntity);
        }
    }

    public void doEditReadBook(ActionEvent actionEvent) {
        int selInd = lstReadBooks.getSelectionModel().getSelectedIndex();
        if (selInd > -1) {
            editBookReadedDialog.setEntity((BookReaded)lstReadBooks.getItems().get(selInd));
            Optional<BookReaded> ret = editBookReadedDialog.showAndWait();
            if (ret.isPresent()) {
                try {
                    sqliteDb.getBookReadedDb().updateBookReaded(ret.get());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                lstReadBooks.getItems().set(selInd, ret.get());
            }
        }
    }

    public void doDeleteReadBook(ActionEvent actionEvent) throws SQLException {
        int selInd = lstReadBooks.getSelectionModel().getSelectedIndex();
        if (selInd > -1) {
            BookReaded item = (BookReaded) lstReadBooks.getItems().get(selInd);
            sqliteDb.getBookReadedDb().deleteBookReaded(item.getId());
            lstReadBooks.getItems().remove(selInd);
        }
    }
}