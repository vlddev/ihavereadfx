package com.vlad.ihaveread;

import com.vlad.ihaveread.db.SqliteDb;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        SqliteDb sqliteDb = new SqliteDb("jdbc:sqlite:/home/volodymrvlod/Dokumente/mydev/db/ihaveread.db");
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);

        MainController mainController = (MainController) fxmlLoader.getController();
        mainController.setSqliteDb(sqliteDb);
        mainController.initListeners();
        mainController.setNewAuthorDialog(new NewAuthorDialog(scene.getWindow(), sqliteDb));

        stage.setTitle("I have read");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}