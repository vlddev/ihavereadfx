package com.vlad.ihaveread;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;

import java.io.IOException;

public class GoodreadsLinkCell implements Callback<TableColumn<PropertySheet.Item, String>, TableCell<PropertySheet.Item, String>> {

    @Override
    public TableCell<PropertySheet.Item, String> call(TableColumn<PropertySheet.Item, String> arg) {
        return new TableCell<>() {

            private final Hyperlink hyperlink = new Hyperlink();

            {
                hyperlink.setOnAction(event -> {
                    String url = "https://www.goodreads.com/book/show/"+getItem();
                    try {
                        new ProcessBuilder("xdg-open", url).start();
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