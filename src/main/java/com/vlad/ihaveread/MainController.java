package com.vlad.ihaveread;

import com.vlad.ihaveread.dao.Author;
import com.vlad.ihaveread.db.SqliteDb;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class MainController {

    private static Logger log = LoggerFactory.getLogger(MainController.class);

    private SqliteDb sqliteDb;
    private Author curAuthor;

    @FXML
    private TextField tfSearchText, tfAuthorName, tfAuthorLang, tfAuthorNote;

    @FXML
    private ListView lstFoundAuthors, lstAuthorNames;

    @FXML
    private TextField tfAuthorNamesName, tfAuthorNamesLang, tfAuthorNamesType;


    public MainController() throws SQLException {
        sqliteDb = new SqliteDb("jdbc:sqlite:/home/volodymrvlod/Dokumente/mydev/db/ihaveread.db");
        //sqliteDb.scanDb();
    }

    public void initListeners() {
        lstFoundAuthors.getSelectionModel().selectedItemProperty().addListener(
                (ChangeListener<Author>) (ov, oldVal, newVal) -> onSelectAuthor(newVal));
        lstFoundAuthors.setCellFactory(callback -> new ListCell<Author>() {
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
    }

    public void doSearchAuthor(ActionEvent actionEvent) throws SQLException {
        clearAuthor();
        String strToFind = tfSearchText.getText().trim();
        log.info("Search for author '{}'", strToFind);
        if (strToFind.length() > 0) {
            List<Author> authors = sqliteDb.getAuthorDb().findByName(sqliteDb.getConnection(), "%"+strToFind+"%");
            if (!authors.isEmpty()) {
                lstFoundAuthors.getItems().clear();
                lstFoundAuthors.getItems().addAll(authors);
            } else {
                log.info("Nothing found");
            }
        }
    }

    public void clearAuthor() {
        tfAuthorName.clear();
        tfAuthorLang.clear();
        tfAuthorNote.clear();
        curAuthor = null;
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
        // TODO get AuthorNames
    }

    public void doSaveAuthor(ActionEvent actionEvent) {
    }

    public void doDeleteAuthor(ActionEvent actionEvent) {
    }

    public void doSaveAuthorName(ActionEvent actionEvent) {
    }

    public void doAddAuthorName(ActionEvent actionEvent) {
    }

    public void doDeleteAuthorName(ActionEvent actionEvent) {
    }
}