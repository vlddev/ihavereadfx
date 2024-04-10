package com.vlad.ihaveread;

import com.vlad.ihaveread.dao.BookName;
import com.vlad.ihaveread.util.Util;
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

    private BookName entity;
    @FXML
    private TextField tfName, tfLang, tfGoodreadId, tfLibFile;

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

                return entity;
            });

            setOnShowing(dialogEvent -> Platform.runLater(() -> tfName.requestFocus()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setEntity(BookName entity) {
        this.entity = entity;
        if (entity != null) {
            setTitle("Edit book name");
            tfName.setText(entity.getName());
            tfLang.setText(entity.getLang());
            tfGoodreadId.setText(entity.getGoodreadsId());
            tfLibFile.setText(entity.getLibFile());
        } else {
            setTitle("New book name");
            tfName.clear();
            tfLang.clear();
            tfGoodreadId.clear();
            tfLibFile.clear();
        }
    }

    @FXML
    private void initialize() {
    }

    @FXML
    private void onCreate(ActionEvent event) {
        try {
            // validate input
            String strName = Util.trimOrEmpty(tfName.getText());
            if (strName.length() == 0) {
                throw new RuntimeException("Name not set");
            }
            String strLang = Util.trimOrEmpty(tfLang.getText());
            if (strLang.length() == 0) {
                throw new RuntimeException("Language not set");
            }

            if (entity == null) { //new book name
                entity = BookName.builder().build();
            }
            entity.setName(strName);
            entity.setLang(strLang);
            entity.setGoodreadsId(Util.trimOrNull(tfGoodreadId.getText()));
            entity.setLibFile(Util.trimOrNull(tfLibFile.getText()));
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