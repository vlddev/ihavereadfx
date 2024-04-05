package com.vlad.ihaveread;

import com.vlad.ihaveread.dao.Author;
import com.vlad.ihaveread.dao.Book;
import com.vlad.ihaveread.dao.BookName;
import com.vlad.ihaveread.dao.BookReaded;
import com.vlad.ihaveread.db.SqliteDb;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NewBookreadedDialog extends Dialog<String> {

    @FXML
    private TextField tfReadTitle, tfReadLang, tfOrigTitle, tfOrigLang, tfPublishDate, tfGenre, tfReadDate, tfMedium, tfScore;

    @FXML
    private TextArea taNote;

    @FXML
    private Button btnAddAuthor, btnDeleteAuthor;

    @FXML
    private ButtonType btnCreate;

    @FXML
    private ListView lstBookAuthors;

    private SelectAuthorDialog selectAuthorDialog;

    private SqliteDb sqliteDb;

    public NewBookreadedDialog(Window owner, SqliteDb sqliteDb) {
        try {
            this.sqliteDb = sqliteDb;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("new-book-readed.fxml"));
            loader.setController(this);

            DialogPane dialogPane = loader.load();
            dialogPane.lookupButton(btnCreate).addEventFilter(ActionEvent.ANY, this::onCreate);

            btnAddAuthor.addEventHandler(ActionEvent.ANY, this::onAddAuthor);
            btnDeleteAuthor.addEventHandler(ActionEvent.ANY, this::onDeleteAuthor);
            setSelectAuthorDialog(new SelectAuthorDialog(owner, sqliteDb));

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

            initOwner(owner);
            initModality(Modality.APPLICATION_MODAL);

            setResizable(true);
            setTitle("New book readed");
            // set to current date
            tfReadDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            setDialogPane(dialogPane);
            setResultConverter(buttonType -> {
                if(!Objects.equals(ButtonBar.ButtonData.OK_DONE, buttonType.getButtonData())) {
                    return null;
                }

                return "";
            });

            setOnShowing(dialogEvent -> Platform.runLater(() -> tfReadTitle.requestFocus()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSelectAuthorDialog(SelectAuthorDialog selectAuthorDialog) {
        this.selectAuthorDialog = selectAuthorDialog;
    }

    @FXML
    private void initialize() {
    }

    @FXML
    private void onAddAuthor(ActionEvent event) {
        Optional<Author> ret = selectAuthorDialog.showAndWait();
        if (ret.isPresent()) {
            lstBookAuthors.getItems().add(ret.get());
        }
    }

    @FXML
    private void onDeleteAuthor(ActionEvent event) {
        int selInd = lstBookAuthors.getSelectionModel().getSelectedIndex();
        lstBookAuthors.getItems().remove(selInd);
    }

    @FXML
    private void onCreate(ActionEvent event) {
        try {
            // validate input
            if (lstBookAuthors.getItems().isEmpty()) {
                throw new RuntimeException("Author(s) not set");
            }
            String strReadTitle = tfReadTitle.getText().trim();
            if (strReadTitle.length() == 0) {
                throw new RuntimeException("Title not set");
            }
            String strReadLang = tfReadLang.getText().trim();
            if (strReadLang.length() == 0) {
                throw new RuntimeException("ReadLang not set");
            }
            String strOrigTitle = tfOrigTitle.getText().trim();
            String strOrigLang = tfReadLang.getText().trim();
            if (strOrigTitle.length() > 0 && strOrigLang.length() == 0) {
                throw new RuntimeException("OrigLang not set");
            }
            int score = 0;
            try {
                score = Integer.parseInt(tfScore.getText().trim());
            } catch (NumberFormatException e) {
                throw new RuntimeException("Score must be number");
            }

            List<BookName> bookNames = new ArrayList<>();
            bookNames.add(BookName.builder().name(strReadTitle).lang(strReadLang).build());
            if (strOrigTitle.length() == 0) {
                strOrigTitle = strReadTitle;
                strOrigLang = strReadLang;
            } else {
                bookNames.add(BookName.builder().name(strOrigTitle).lang(strOrigLang).build());
            }

            Book book = Book.builder()
                    .title(strOrigTitle)
                    .lang(strOrigLang)
                    .genre(tfGenre.getText().trim())
                    .publishDate(tfPublishDate.getText().trim())
                    .lang(strOrigLang)
                    .note(taNote.getText().trim())
                    .build();
            sqliteDb.getConnection().setAutoCommit(false);
            book = sqliteDb.getBookDb().insertBook(book);
            for (BookName bookName : bookNames) {
                bookName.setBookId(book.getId());
            }
            sqliteDb.getBookDb().insertBookNames(bookNames);
            BookReaded bookReaded = BookReaded.builder()
                    .bookId(book.getId())
                    .langRead(strReadLang)
                    .dateRead(tfReadDate.getText().trim())
                    .medium(tfMedium.getText().trim())
                    .score(score)
                    .build();
            sqliteDb.getBookReadedDb().insertBookReaded(bookReaded);
            sqliteDb.getBookDb().insertBookAuthors(book, lstBookAuthors.getItems());

            sqliteDb.getConnection().commit();
            sqliteDb.getConnection().setAutoCommit(true);
            return;
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(getDialogPane().getScene().getWindow());
            alert.initModality(Modality.APPLICATION_MODAL);

            alert.setResizable(true);

            alert.setTitle(getTitle());
            alert.setHeaderText(null);
            alert.setContentText(e.getLocalizedMessage());
            alert.show();
        } catch (SQLException e) {
            try {
                sqliteDb.getConnection().rollback();
                sqliteDb.getConnection().setAutoCommit(true);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        event.consume();
    }
}