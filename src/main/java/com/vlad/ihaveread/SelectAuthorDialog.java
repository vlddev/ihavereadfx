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
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class SelectAuthorDialog extends Dialog<Author> {

    @FXML
    private TextField tfSearch;

    @FXML
    private Button btnSearch;

    @FXML
    private ButtonType btnOk;

    @FXML
    private ListView lstAuthors;

    private SqliteDb sqliteDb;

    public SelectAuthorDialog(Window owner, SqliteDb sqliteDb) {
        try {
            this.sqliteDb = sqliteDb;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("select-author.fxml"));
            loader.setController(this);

            DialogPane dialogPane = loader.load();
            dialogPane.lookupButton(btnOk).addEventFilter(ActionEvent.ANY, this::onSelect);

            btnSearch.addEventHandler(ActionEvent.ANY, this::onSearchAuthor);

            lstAuthors.setCellFactory(callback -> new ListCell<Author>() {
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
            setTitle("New author");
            setDialogPane(dialogPane);
            setResultConverter(buttonType -> {
                if (!Objects.equals(ButtonBar.ButtonData.OK_DONE, buttonType.getButtonData())) {
                    return null;
                }

                return (Author) lstAuthors.getSelectionModel().getSelectedItem();
            });

            setOnShowing(dialogEvent -> Platform.runLater(() -> tfSearch.requestFocus()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void initialize() {
    }
    @FXML
    private void onSearchAuthor(ActionEvent event) {
        String strToFind = tfSearch.getText().trim();
        if (strToFind.length() > 0) {
            List<Author> authors = null;
            try {
                authors = sqliteDb.getAuthorDb().findByName("%"+strToFind+"%");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            lstAuthors.getItems().clear();
            if (!authors.isEmpty()) {
                lstAuthors.getItems().addAll(authors);
            }
        }
    }

    @FXML
    private void onSelect(ActionEvent event) {
        if ( lstAuthors.getSelectionModel().getSelectedItem() != null) {
            return;
        }
        event.consume();
    }
}