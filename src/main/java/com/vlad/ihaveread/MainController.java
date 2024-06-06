package com.vlad.ihaveread;

import com.vlad.ihaveread.dao.*;
import com.vlad.ihaveread.db.SqliteDb;
import com.vlad.ihaveread.util.Util;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javafx.scene.input.MouseButton;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private SqliteDb sqliteDb;
    private Author curAuthor;
    private Book curBook;

    private NewAuthorDialog newAuthorDialog;
    private NewBookreadedDialog newBookreadedDialog;
    private SelectAuthorDialog selectAuthorDialog;
    private EditBookNameDialog editBookNameDialog;
    private EditAuthorNameDialog editAuthorNameDialog;
    private EditBookReadedDialog editBookReadedDialog;

    @FXML
    private TextField tfSearchText, tfAuthorName, tfAuthorLang, tfAuthorNote;

    @FXML
    private ListView<Author> lstFoundAuthors, lstBookAuthors;

    @FXML
    private ListView<Book> lstFoundBooks;

    @FXML
    private TableView<AuthorName> lstAuthorNames;

    @FXML
    private TableView<BookName> lstBookNames;

    @FXML
    private Tab tabBook, tabAuthor, tabReaded;

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField tfSearchReadedText;
    @FXML
    private TableView<BookReadedTblRow> tvFoundReadBooks;
    @FXML
    private TableView<BookReaded> lstReadBooks;

    @FXML
    private TextField tfBookSearchText, tfBookTitle, tfBookLang, tfPublishDate, tfGenre;

    @FXML
    private TextArea taBookNote;

    @FXML
    private Label lblStatus, lblAuthorStatus, lblBookStatus, lblToolsStatus;

    @FXML
    private Label lblAuthorCount, lblBookCount, lblBookReadedCount;

    public void setSqliteDb(SqliteDb sqliteDb) {
        this.sqliteDb = sqliteDb;
    }

    public void initListeners() {
        lstFoundAuthors.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldVal, newVal) -> onSelectAuthor(newVal));
        lstFoundAuthors.setCellFactory(callback -> new PropertyListCellFactory<>("name"));
        lstFoundBooks.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldVal, newVal) -> onSelectBookName(newVal));
        lstFoundBooks.setCellFactory(callback -> new PropertyListCellFactory<>("title"));
        lstBookAuthors.setCellFactory(callback -> new ListCell<>() {
            @Override
            protected void updateItem(Author item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                    setOnMouseClicked(mouseClickedEvent -> {
                        if (mouseClickedEvent.getButton().equals(MouseButton.PRIMARY) && mouseClickedEvent.getClickCount() == 2) {
                            showInAuthorTab(item);
                        }
                    });
                }
            }
        });
        // double-click on table row - show edit dialog
        lstBookNames.setRowFactory(tv -> {
            TableRow<BookName> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    doEditBookName();
                }
            });
            return row ;
        });
        // double-click on table row - show edit dialog
        lstAuthorNames.setRowFactory(tv -> {
            TableRow<AuthorName> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    doEditAuthorName();
                }
            });
            return row ;
        });
        // double-click on table row - show book in Book-Tab
        tvFoundReadBooks.setRowFactory(tv -> {
            TableRow<BookReadedTblRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    BookReadedTblRow rowData = row.getItem();
                    showInBookTab(rowData);
                }
            });
            return row ;
        });
        // double-click on table row - show edit dialog
        lstReadBooks.setRowFactory(tv -> {
            TableRow<BookReaded> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    //BookReaded rowData = row.getItem();
                    doEditReadBook();
                }
            });
            return row ;
        });
    }

    public void initComponents(Scene scene) {
        newAuthorDialog = new NewAuthorDialog(scene.getWindow(), sqliteDb);
        newBookreadedDialog = new NewBookreadedDialog(scene.getWindow(), sqliteDb);
        selectAuthorDialog = new SelectAuthorDialog(scene.getWindow(), sqliteDb);
        editBookNameDialog = new EditBookNameDialog(scene.getWindow());
        editAuthorNameDialog = new EditAuthorNameDialog(scene.getWindow());
        editBookReadedDialog = new EditBookReadedDialog(scene.getWindow());
    }

    public void initData() throws SQLException {
        lblAuthorCount.setText(""+sqliteDb.getAuthorDb().getAuthorCount());
        lblBookCount.setText(""+sqliteDb.getBookDb().getBookCount());
        lblBookReadedCount.setText(""+sqliteDb.getBookReadedDb().getBookReadedCount());
    }

    public void doSearchAuthor() throws SQLException {
        clearAuthor();
        String strToFind = tfSearchText.getText().trim();
        if (strToFind.length() > 0) {
            List<Author> authors = sqliteDb.getAuthorDb().findByName("%"+strToFind+"%");
            lstFoundAuthors.getItems().clear();
            if (!authors.isEmpty()) {
                lblAuthorStatus.setText(""+authors.size()+" row(s)");
                lstFoundAuthors.getItems().addAll(authors);
                lstFoundAuthors.getSelectionModel().select(0);
                lstFoundAuthors.requestFocus();
            } else {
                lblAuthorStatus.setText("Nothing found");
            }
        }
    }

    public void showAuthorBooks() {
        if (curAuthor != null) {
            tvFoundReadBooks.getItems().clear();
            tfSearchReadedText.setText(curAuthor.getName());
            doSearchReadedByAuthor();
            tabPane.getSelectionModel().select(tabReaded);
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
            lstAuthorNames.getItems().clear();
            lstAuthorNames.getItems().addAll(authorNames);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void doAddAuthor() {
        newAuthorDialog.showAndWait();
    }

    public void doSaveAuthor() {
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

    public void doDeleteAuthor() {
        if (curAuthor != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm deletion");
            alert.setHeaderText("Delete "+curAuthor.getName()+"?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK){
                try {
                    sqliteDb.getAuthorDb().deleteAuthor(curAuthor.getId());
                    doSearchAuthor();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void doEditAuthorName() {
        int selInd = lstAuthorNames.getSelectionModel().getSelectedIndex();
        if (selInd > -1) {
            String oldName = lstAuthorNames.getItems().get(selInd).getName();
            editAuthorNameDialog.setEntity(lstAuthorNames.getItems().get(selInd));
            Optional<AuthorName> ret = editAuthorNameDialog.showAndWait();
            if (ret.isPresent()) {
                try {
                    sqliteDb.getAuthorDb().updateAuthorName(oldName, ret.get());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                lstAuthorNames.getItems().set(selInd, ret.get());
            }
        }
    }

    public void doDeleteAuthorName() throws SQLException {
        int selInd = lstAuthorNames.getSelectionModel().getSelectedIndex();
        if (selInd > -1) {
            AuthorName item = lstAuthorNames.getItems().get(selInd);
            sqliteDb.getAuthorDb().deleteAuthorName(item);
            lstAuthorNames.getItems().remove(selInd);
        }
    }

    public void doAddAuthorName() {
        if (curAuthor != null) {
            editAuthorNameDialog.setEntity(null);
            Optional<AuthorName> ret = editAuthorNameDialog.showAndWait();
            if (ret.isPresent()) {
                ret.get().setAuthorId(curAuthor.getId());
                List<AuthorName> authorNames = new ArrayList<>(1);
                authorNames.add(ret.get());
                try {
                    sqliteDb.getAuthorDb().insertAuthorNames(authorNames);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                loadAuthorNames(curAuthor.getId());
            }
        }
    }

    public void doSearchReadedByTitle() {
        doSearchReadedBy(sqliteDb.getBookReadedDb()::getReadedBooksByTitle);
    }

    public void doSearchReadedByAuthor() {
        doSearchReadedBy(sqliteDb.getBookReadedDb()::getReadedBooksByAuthor);
    }

    public void doSearchReadedByYear() {
        doSearchReadedBy(sqliteDb.getBookReadedDb()::getReadedBooksByYear);
    }

    private void doSearchReadedBy(Function<String, List<BookReadedTblRow>> getBy) {
        String strToFind = tfSearchReadedText.getText().trim();
        if (strToFind.length() > 0) {
            List<BookReadedTblRow> books = getBy.apply(strToFind);
            tvFoundReadBooks.getItems().clear();
            if (!books.isEmpty()) {
                lblStatus.setText(""+books.size()+" row(s)");
                tvFoundReadBooks.getItems().addAll(books);
            } else {
                lblStatus.setText("Nothing found");
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

    public void doSearchBook() throws SQLException {
        clearBook();
        String strToFind = tfBookSearchText.getText().trim();
        if (strToFind.length() > 0) {
            List<Book> books = sqliteDb.getBookDb().findByName("%"+strToFind+"%");
            lstFoundBooks.getItems().clear();
            if (!books.isEmpty()) {
                lblBookStatus.setText(""+books.size()+" row(s)");
                lstFoundBooks.getItems().addAll(books);
                lstFoundBooks.getSelectionModel().select(0);
                lstFoundBooks.requestFocus();
            } else {
                lblBookStatus.setText("Nothing found");
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

    public void showInAuthorTab(Author entity) {
        lstFoundAuthors.getItems().clear();
        lstFoundAuthors.getItems().add(entity);
        lstFoundAuthors.getSelectionModel().select(entity);
        tabPane.getSelectionModel().select(tabAuthor);
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
            List<Author> authors = sqliteDb.getAuthorDb().getByBookId(book.getId());
            lstBookAuthors.getItems().addAll(authors);
            lstBookNames.getItems().addAll(sqliteDb.getBookDb().getBookNameByBookId(book.getId(), authors.get(0)));
            lstReadBooks.getItems().addAll(sqliteDb.getBookReadedDb().getByBookId(book.getId()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void doAddBook() {
        newBookreadedDialog.showAndWait();
    }

    public void doAddBookAuthor() throws SQLException {
        Optional<Author> ret = selectAuthorDialog.showAndWait();
        if (ret.isPresent()) {
            if (!lstBookAuthors.getItems().contains(ret.get())) {
                sqliteDb.getBookDb().insertBookAuthor(curBook.getId(), ret.get().getId());
                lstBookAuthors.getItems().add(ret.get());
            }
        }
    }

    public void doDeleteBookAuthor() throws SQLException {
        int selInd = lstBookAuthors.getSelectionModel().getSelectedIndex();
        Author author = lstBookAuthors.getItems().get(selInd);
        sqliteDb.getBookDb().deleteBookAuthor(curBook.getId(), author.getId());
        lstBookAuthors.getItems().remove(selInd);
    }

    public void doSaveBook() throws SQLException {
        curBook.setLang(tfBookLang.getText().trim());
        curBook.setTitle(tfBookTitle.getText().trim());
        curBook.setGenre(tfGenre.getText().trim());
        curBook.setPublishDate(tfPublishDate.getText().trim());
        curBook.setNote(Util.trimOrNull(taBookNote.getText()));
        sqliteDb.getBookDb().updateBook(curBook);
    }

    public void doDeleteBook() {
        if (curBook != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm deletion");
            alert.setHeaderText("Delete "+curBook.getTitle()+"?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK){
                try {
                    sqliteDb.getBookDb().deleteBook(curBook.getId());
                    doSearchBook();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void doAddBookName() throws SQLException {
        editBookNameDialog.setEntity(null);
        Optional<BookName> ret = editBookNameDialog.showAndWait();
        if (ret.isPresent()) {
            ret.get().setBookId(curBook.getId());
            BookName newEntity = sqliteDb.getBookDb().insertBookName(ret.get());
            Author author = lstBookAuthors.getItems().get(0);
            newEntity.setBookLibFile(sqliteDb.getBookDb().composeBookLibFile(newEntity, author));
            lstBookNames.getItems().add(newEntity);
        }
    }

    public void doEditBookName() {
        int selInd = lstBookNames.getSelectionModel().getSelectedIndex();
        if (selInd > -1) {
            editBookNameDialog.setEntity(lstBookNames.getItems().get(selInd));
            Optional<BookName> ret = editBookNameDialog.showAndWait();
            if (ret.isPresent()) {
                try {
                    sqliteDb.getBookDb().updateBookName(ret.get());
                    //reload BookNames
                    lstBookNames.getItems().clear();
                    Author author = lstBookAuthors.getItems().get(0);
                    lstBookNames.getItems().addAll(sqliteDb.getBookDb().getBookNameByBookId(ret.get().getBookId(), author));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void doFindLibFile() {
        int selInd = lstBookNames.getSelectionModel().getSelectedIndex();
        if (selInd > -1) {
            BookName bn = lstBookNames.getItems().get(selInd);
            BookLibFile bookLibFile = bn.getBookLibFile();
            if (bookLibFile.getLibFile() == null || bookLibFile.getLibFile().length() == 0) {
                String bookDir = MainApplication.LIB_ROOT+bookLibFile.getBookDir();
                if (Files.isDirectory(Path.of(bookDir))) {
                    // get files similar to book name from dir
                    List<String> bookFiles = Util.getSimilarFiles(bookLibFile.getBookName(), bookDir);
                    if (bookFiles.size() > 0) {
                        if (bookFiles.size() == 1) {
                            String libFile = Path.of(bookLibFile.getBookDir(),bookFiles.get(0)).toString();
                            bookLibFile.setLibFile(libFile);
                            bn.setLibFile(libFile);
                        } else {
                            // show dialog to select one of files
                            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(bookFiles.get(0), bookFiles);
                            Optional<String> selected = choiceDialog.showAndWait();
                            if (selected.isPresent()) {
                                String libFile = Path.of(bookLibFile.getBookDir(), selected.get()).toString();
                                bookLibFile.setLibFile(libFile);
                                bn.setLibFile(libFile);
                            }
                        }
                        // update LibFile in BookNames
                        try {
                            if (bn.getLibFile() != null && bn.getLibFile().length() > 0) {
                                sqliteDb.getBookDb().updateBookName(bn);
                            }
                            //reload BookNames
                            lstBookNames.getItems().clear();
                            Author author = lstBookAuthors.getItems().get(0);
                            lstBookNames.getItems().addAll(sqliteDb.getBookDb().getBookNameByBookId(bn.getBookId(), author));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Util.infoAlert("Not found", "No similar files").show();
                    }
                } else {
                    Util.warningAlert("Warning", "Folder '"+bookLibFile.getBookDir()+"' not exist").show();
                }
            }
        } else {
            Util.infoAlert("Select record", "Record not selected").show();
        }
    }

    public void doDeleteBookName() throws SQLException {
        int selInd = lstBookNames.getSelectionModel().getSelectedIndex();
        if (selInd > -1) {
            BookName item = lstBookNames.getItems().get(selInd);
            sqliteDb.getBookDb().deleteBookName(item.getId());
            lstBookNames.getItems().remove(selInd);
        }
    }

    public void doAddReadBook() throws SQLException {
        editBookReadedDialog.setEntity(null);
        Optional<BookReaded> ret = editBookReadedDialog.showAndWait();
        if (ret.isPresent()) {
            ret.get().setBookId(curBook.getId());
            BookReaded newEntity = sqliteDb.getBookReadedDb().insertBookReaded(ret.get());
            lstReadBooks.getItems().add(newEntity);
        }
    }

    public void doEditReadBook() {
        int selInd = lstReadBooks.getSelectionModel().getSelectedIndex();
        if (selInd > -1) {
            editBookReadedDialog.setEntity(lstReadBooks.getItems().get(selInd));
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

    public void doDeleteReadBook() throws SQLException {
        int selInd = lstReadBooks.getSelectionModel().getSelectedIndex();
        if (selInd > -1) {
            BookReaded item = lstReadBooks.getItems().get(selInd);
            sqliteDb.getBookReadedDb().deleteBookReaded(item.getId());
            lstReadBooks.getItems().remove(selInd);
        }
    }

    public void doBackupDb() {
        String sqliteDb = MainApplication.DB_FILE;
        String dumpFile = sqliteDb + "_dump.sql";
        String strStatus = "";
        try {
            ProcessBuilder builder = new ProcessBuilder("sqlite3", "-batch", sqliteDb, ".dump").inheritIO()
                    .redirectOutput(new File(dumpFile));
            Process process = builder.start();
            int exitCode = process.waitFor();
            strStatus = "Done. Exit code = "+exitCode;
        } catch (InterruptedException | IOException e) {
            strStatus = "Error. "+e.getMessage();
            //throw new RuntimeException(e);
        }
        lblToolsStatus.setText(strStatus);
    }
}