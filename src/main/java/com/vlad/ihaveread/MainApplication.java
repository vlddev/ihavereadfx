package com.vlad.ihaveread;

import com.vlad.ihaveread.db.SqliteDb;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainApplication extends Application {

    public static final String DEFAULT_DB_FILE = "./ihaveread.db";
    public static final String PARAM_DB_FILE = "dbfile";

    @Override
    public void start(Stage stage) throws Exception {
        String strDbFile = System.getProperty(PARAM_DB_FILE, DEFAULT_DB_FILE);
        File dbFile = new File(strDbFile);
        if (!dbFile.exists()) {
            throw new RuntimeException("DB file "+strDbFile+" not exist.");
        }
        SqliteDb sqliteDb = new SqliteDb("jdbc:sqlite:"+strDbFile);
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1600, 1000);

        MainController mainController = (MainController) fxmlLoader.getController();
        mainController.setSqliteDb(sqliteDb);
        mainController.initListeners();
        mainController.initComponents(scene);

        stage.getIcons().add(new Image(MainApplication.class.getResourceAsStream("icon.png")));
        stage.setTitle("I have read");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}