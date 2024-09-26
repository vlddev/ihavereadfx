package com.vlad.ihaveread;

import com.vlad.ihaveread.dao.BookReaded;
import com.vlad.ihaveread.util.Util;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class EditBookReadedDialog extends Dialog<BookReaded> {

    private BookReaded entity;
    @FXML
    private TextField tfReadLang, tfMedium, tfScore;
    @FXML
    private DatePicker dpReadDate;
    @FXML
    private TextArea taNote;

    @FXML
    private ButtonType btnCreate;

    public EditBookReadedDialog(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("edit-book-readed.fxml"));
            loader.setController(this);

            DialogPane dialogPane = loader.load();
            dialogPane.lookupButton(btnCreate).addEventFilter(ActionEvent.ANY, this::onCreate);

            initOwner(owner);
            initModality(Modality.APPLICATION_MODAL);

            setResizable(true);
            setTitle("New");
            setDialogPane(dialogPane);
            setResultConverter(buttonType -> {
                if(!Objects.equals(ButtonBar.ButtonData.OK_DONE, buttonType.getButtonData())) {
                    return null;
                }

                return entity;
            });

            setOnShowing(dialogEvent -> Platform.runLater(() -> tfReadLang.requestFocus()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setEntity(BookReaded entity) {
        this.entity = entity;
        if (entity != null) {
            setTitle("Edit");
            tfReadLang.setText(entity.getLangRead());
            dpReadDate.setValue(LocalDate.parse(entity.getDateRead(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            tfMedium.setText(entity.getMedium());
            tfScore.setText(entity.getScore().toString());
            taNote.setText(entity.getNote());
        } else {
            setTitle("New");
            tfReadLang.clear();
            dpReadDate.setValue(LocalDate.now());
            tfMedium.clear();
            tfScore.clear();
            taNote.clear();
        }
    }

    @FXML
    private void onCreate(ActionEvent event) {
        try {
            // validate input
            String strDate = dpReadDate.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (strDate.length() == 0) {
                throw new RuntimeException("Date not set");
            }
            String strLang = Util.trimOrEmpty(tfReadLang.getText());
            if (strLang.length() == 0) {
                throw new RuntimeException("Language not set");
            }
            int score;
            try {
                score = Integer.parseInt(Util.trimOrNull(tfScore.getText()));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Score must be number");
            }

            if (entity == null) { //new book name
                entity = BookReaded.builder().build();
            }
            entity.setDateRead(strDate);
            entity.setLangRead(strLang);
            entity.setMedium(Util.trimOrNull(tfMedium.getText()));
            entity.setNote(Util.trimOrNull(taNote.getText()));
            entity.setScore(score);

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