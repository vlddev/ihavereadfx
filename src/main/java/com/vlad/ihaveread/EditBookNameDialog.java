package com.vlad.ihaveread;

import com.vlad.ihaveread.dao.Author;
import com.vlad.ihaveread.dao.BookName;
import com.vlad.ihaveread.db.SqliteDb;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Objects;

public class EditBookNameDialog extends Dialog<BookName> {

    private BookName bookName;
    @FXML
    private TextField tfName, tfLang;

    @FXML
    private ButtonType btnCreate;

    public EditBookNameDialog(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("edit-book-name.fxml"));
            loader.setController(this);

            DialogPane dialogPane = loader.load();
            dialogPane.lookupButton(btnCreate).addEventFilter(ActionEvent.ANY, this::onCreate);

            initOwner(owner);
            initModality(Modality.APPLICATION_MODAL);

            setResizable(true);
            setTitle("New book name");
            setDialogPane(dialogPane);
            setResultConverter(buttonType -> {
                if(!Objects.equals(ButtonBar.ButtonData.OK_DONE, buttonType.getButtonData())) {
                    return null;
                }

                return bookName;
            });

            setOnShowing(dialogEvent -> Platform.runLater(() -> tfName.requestFocus()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setBookName(BookName bookName) {
        this.bookName = bookName;
        if (bookName != null) {
            setTitle("Edit book name");
            tfName.setText(bookName.getName());
            tfLang.setText(bookName.getLang());
        } else {
            setTitle("New book name");
            tfName.clear();
            tfLang.clear();
        }
    }

    @FXML
    private void initialize() {
    }

    @FXML
    private void onCreate(ActionEvent event) {
        try {
            // validate input
            String strName = tfName.getText().trim();
            if (strName.length() == 0) {
                throw new RuntimeException("Name not set");
            }
            String strLang = tfLang.getText().trim();
            if (strLang.length() == 0) {
                throw new RuntimeException("Language not set");
            }

            if (bookName == null) { //new book name
                bookName = BookName.builder().name(strName)
                        .lang(strLang).build();
            } else {
                bookName.setName(strName);
                bookName.setLang(strLang);
            }
            return;
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(getDialogPane().getScene().getWindow());
            alert.initModality(Modality.APPLICATION_MODAL);

            alert.setResizable(true);

            alert.setTitle(getTitle());
            alert.setHeaderText(null);
            alert.setContentText(e.getLocalizedMessage());

            alert.show();
        }

        event.consume();
    }
}