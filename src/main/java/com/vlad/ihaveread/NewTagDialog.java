package com.vlad.ihaveread;

import com.vlad.ihaveread.dao.Tag;
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

public class NewTagDialog extends Dialog<String> {

    @FXML
    private TextField tfTagNameEn, tfTagNameUk;

    @FXML
    private ButtonType btnCreate;

    private SqliteDb sqliteDb;

    public NewTagDialog(Window owner, SqliteDb sqliteDb) {
        try {
            this.sqliteDb = sqliteDb;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("new-tag.fxml"));
            loader.setController(this);

            DialogPane dialogPane = loader.load();
            dialogPane.lookupButton(btnCreate).addEventFilter(ActionEvent.ANY, this::onCreate);

            initOwner(owner);
            initModality(Modality.APPLICATION_MODAL);

            setResizable(true);
            setTitle("New tag");
            setDialogPane(dialogPane);
            setResultConverter(buttonType -> {
                if(!Objects.equals(ButtonBar.ButtonData.OK_DONE, buttonType.getButtonData())) {
                    return null;
                }

                return "";
            });

            setOnShowing(dialogEvent -> Platform.runLater(() -> tfTagNameEn.requestFocus()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void initialize() {
    }

    private void clear() {
        tfTagNameEn.clear();
        tfTagNameUk.clear();
    }

    @FXML
    private void onCreate(ActionEvent event) {
        try {
            // validate input
            String strVal = tfTagNameEn.getText().trim();
            if (strVal.length() == 0) {
                throw new RuntimeException("NameEn not set");
            }
            strVal = tfTagNameUk.getText().trim();
            if (strVal.length() == 0) {
                throw new RuntimeException("NameUk not set");
            }

            Tag newTag = sqliteDb.getBookDb().insertTag(Tag.builder().nameEn(tfTagNameEn.getText().trim())
                    .nameUk(tfTagNameUk.getText().trim()).build());
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