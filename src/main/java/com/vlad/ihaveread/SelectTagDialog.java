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
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class SelectTagDialog extends Dialog<Tag> {

    @FXML
    private TextField tfSearch;

    @FXML
    private Button btnSearch;

    @FXML
    private ButtonType btnOk;

    @FXML
    private ListView<Tag> lstTags;

    private SqliteDb sqliteDb;

    public SelectTagDialog(Window owner, SqliteDb sqliteDb) {
        try {
            this.sqliteDb = sqliteDb;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("select-tag.fxml"));
            loader.setController(this);

            DialogPane dialogPane = loader.load();
            dialogPane.lookupButton(btnOk).addEventFilter(ActionEvent.ANY, this::onSelect);

            btnSearch.addEventHandler(ActionEvent.ANY, this::onSearchAuthor);
            setOnShown(this::clear);

            //lstTags.setCellFactory(callback -> new PropertyListCellFactory<>("name"));

            initOwner(owner);
            initModality(Modality.APPLICATION_MODAL);

            setResizable(true);
            setTitle("Tag");
            setDialogPane(dialogPane);
            setResultConverter(buttonType -> {
                if (!Objects.equals(ButtonBar.ButtonData.OK_DONE, buttonType.getButtonData())) {
                    return null;
                }

                return (Tag) lstTags.getSelectionModel().getSelectedItem();
            });

            setOnShowing(dialogEvent -> Platform.runLater(() -> tfSearch.requestFocus()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void clear(DialogEvent dialogEvent) {
        tfSearch.clear();
        lstTags.getItems().clear();
    }

    @FXML
    private void initialize() {
    }

    @FXML
    private void onSearchAuthor(ActionEvent event) {
        String strToFind = tfSearch.getText().trim();
        if (strToFind.length() > 0) {
            List<Tag> tags = null;
            try {
                tags = sqliteDb.getBookDb().findTagLikeName("%"+strToFind+"%");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            lstTags.getItems().clear();
            if (!tags.isEmpty()) {
                lstTags.getItems().addAll(tags);
            }
        }
    }

    @FXML
    private void onSelect(ActionEvent event) {
        if ( lstTags.getSelectionModel().getSelectedItem() != null) {
            return;
        }
        event.consume();
    }
}