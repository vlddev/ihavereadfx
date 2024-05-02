package com.vlad.ihaveread;

import com.vlad.ihaveread.db.SqliteDb;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

public class MainApplication extends Application {

    public static final String PARAM_DB_FILE = "dbfile";
    public static final String PARAM_LIB_ROOT = "libroot";
    public static final String DEFAULT_DB_FILE = "./ihaveread.db";
    public static final String DEFAULT_LIB_ROOT = "./";

    public static String LIB_ROOT = DEFAULT_LIB_ROOT;

    @Override
    public void start(Stage stage) throws Exception {
        LIB_ROOT = System.getProperty(PARAM_LIB_ROOT, DEFAULT_LIB_ROOT);
        String strDbFile = System.getProperty(PARAM_DB_FILE, DEFAULT_DB_FILE);
        File dbFile = new File(strDbFile);
        if (!dbFile.exists()) {
            throw new RuntimeException("DB file "+strDbFile+" not exist.");
        }
        SqliteDb sqliteDb = new SqliteDb("jdbc:sqlite:"+strDbFile);
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1600, 1000);

        stage.getIcons().add(new Image(Objects.requireNonNull(MainApplication.class.getResourceAsStream("icon.png"))));
        stage.setTitle("I have read");
        stage.setScene(scene);

        MainController mainController = fxmlLoader.getController();
        mainController.setSqliteDb(sqliteDb);
        mainController.initListeners();
        mainController.initComponents(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}