package com.vlad.ihaveread;

import com.vlad.ihaveread.db.SqliteDb;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    public static final String DEFAULT_DB_FILE = "./ihaveread.db";
    public static final String PARAM_DB_FILE = "dbfile";

    @Override
    public void start(Stage stage) throws Exception {
        String dbFile = DEFAULT_DB_FILE;
        try {
            dbFile = System.getenv(PARAM_DB_FILE);
            //SqliteDb sqliteDb = new SqliteDb("jdbc:sqlite:/home/volodymrvlod/Dokumente/mydev/db/ihaveread.db");
            SqliteDb sqliteDb = new SqliteDb("jdbc:sqlite:"+dbFile);
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1000, 700);

            MainController mainController = (MainController) fxmlLoader.getController();
            mainController.setSqliteDb(sqliteDb);
            mainController.initListeners();
            mainController.initComponents(scene);

            stage.setTitle("I have read");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {

        }
    }

    public static void main(String[] args) {
        launch();
    }
}