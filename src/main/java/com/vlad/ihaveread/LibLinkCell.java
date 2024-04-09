package com.vlad.ihaveread;

import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LibLinkCell implements Callback<TableColumn<PropertySheet.Item, String>, TableCell<PropertySheet.Item, String>> {

    @Override
    public TableCell<PropertySheet.Item, String> call(TableColumn<PropertySheet.Item, String> arg) {
        return new TableCell<>() {

            private final Hyperlink hyperlink = new Hyperlink();

            {
                hyperlink.setOnAction(event -> {
                    String file = MainApplication.LIB_ROOT+getItem();
                    try {
                        if (Files.exists(Path.of(file))) {
                            new ProcessBuilder("xdg-open", file).start();
                        } else {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Warning");
                            alert.setHeaderText("File not exist");
                            alert.show();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    hyperlink.setText(url);
                    setGraphic(hyperlink);
                }
            }
        };
    }
}