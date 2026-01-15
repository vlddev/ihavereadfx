package com.vlad.ihaveread;

import com.vlad.ihaveread.dao.BookName;
import com.vlad.ihaveread.dao.BookReaded;
import com.vlad.ihaveread.util.Util;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class EditBookReadedDialog extends Dialog<BookReaded> {

    private BookReaded entity;
    private List<BookName> bookNames;
    @FXML
    private ComboBox<BookName> cbBookNames;
    @FXML
    private TextField tfMedium, tfScore;
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

            setOnShowing(dialogEvent -> Platform.runLater(() -> cbBookNames.requestFocus()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setEntity(BookReaded entity, List<BookName> bookNames) {
        this.entity = entity;
        this.bookNames = bookNames;
        cbBookNames.setItems((ObservableList) bookNames);
        if (entity != null) {
            setTitle("Edit");
            cbBookNames.getSelectionModel().select(bookNames.stream().filter(e->e.getId().equals(entity.getBookNameId())).findFirst().get());
            if (entity.getDateRead() != null) {
                dpReadDate.setValue(LocalDate.parse(entity.getDateRead(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
            tfMedium.setText(entity.getMedium());
            tfScore.setText(entity.getScore().toString());
            taNote.setText(entity.getNote());
        } else {
            setTitle("New");
            cbBookNames.getSelectionModel().selectFirst();
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
            if (strDate.isEmpty()) {
                throw new RuntimeException("Date not set");
            }
            // TODO: check cbBookNames selection
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
            entity.setBookNameId(cbBookNames.getSelectionModel().getSelectedItem().getId());
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