package com.vlad.ihaveread;

import com.vlad.ihaveread.dao.Author;
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

public class NewAuthorDialog extends Dialog<String> {

    @FXML
    private TextField tfAuthorNames, tfAuthorSurname, tfAuthorLang, tfAuthorNote;

    @FXML
    private ButtonType btnCreate;

    private SqliteDb sqliteDb;

    public NewAuthorDialog(Window owner, SqliteDb sqliteDb) {
        try {
            this.sqliteDb = sqliteDb;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("new-author.fxml"));
            loader.setController(this);

            DialogPane dialogPane = loader.load();
            dialogPane.lookupButton(btnCreate).addEventFilter(ActionEvent.ANY, this::onCreate);

            initOwner(owner);
            initModality(Modality.APPLICATION_MODAL);

            setResizable(true);
            setTitle("New author");
            setDialogPane(dialogPane);
            setResultConverter(buttonType -> {
                if(!Objects.equals(ButtonBar.ButtonData.OK_DONE, buttonType.getButtonData())) {
                    return null;
                }

                return "";
            });

            setOnShowing(dialogEvent -> Platform.runLater(() -> tfAuthorNames.requestFocus()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void initialize() {
    }

    private void clear() {
        tfAuthorNames.clear();
        tfAuthorSurname.clear();
        tfAuthorLang.clear();
        tfAuthorNote.clear();
    }

    @FXML
    private void onCreate(ActionEvent event) {
        try {
            // validate input
            String strVal = tfAuthorNames.getText().trim();
            if (strVal.length() == 0) {
                throw new RuntimeException("Names not set");
            }
            strVal = tfAuthorSurname.getText().trim();
            if (strVal.length() == 0) {
                throw new RuntimeException("Surname not set");
            }

            Author newAuthor = sqliteDb.getAuthorDb().insertAuthor(tfAuthorSurname.getText().trim(), tfAuthorNames.getText().trim(),
                    tfAuthorLang.getText().trim(), tfAuthorNote.getText().trim());
            //cleanup all fields
            clear();
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