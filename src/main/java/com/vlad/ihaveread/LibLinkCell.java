package com.vlad.ihaveread;

import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LibLinkCell<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {

    @Override
    public TableCell<S, T> call(TableColumn<S, T> arg) {
        return new TableCell<>() {

            private final Hyperlink hyperlink = new Hyperlink();

            {
                hyperlink.setOnAction(event -> {
                    String file = MainApplication.LIB_ROOT+getItem();
                    try {
                        if (Files.exists(Path.of(file))) {
                            if (file.toLowerCase().endsWith(".epub") ||
                                file.toLowerCase().endsWith(".fb2") ||
                                file.toLowerCase().endsWith(".fb2.zip")
                            ) {
                                new ProcessBuilder("foliate", file).start();
                            } else {
                                new ProcessBuilder("xdg-open", file).start();
                            }
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
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    hyperlink.setText(item == null ? "" : item.toString());
                    setGraphic(hyperlink);
                }
            }
        };
    }
}