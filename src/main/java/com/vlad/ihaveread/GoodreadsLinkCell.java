package com.vlad.ihaveread;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;

import java.io.IOException;
import java.net.MalformedURLException;

public class GoodreadsLinkCell implements Callback<TableColumn<PropertySheet.Item, String>, TableCell<PropertySheet.Item, String>> {

    @Override
    public TableCell<PropertySheet.Item, String> call(TableColumn<PropertySheet.Item, String> arg) {
        TableCell<PropertySheet.Item, String> cell = new TableCell<>() {

            private final Hyperlink hyperlink = new Hyperlink();

            {
                hyperlink.setOnAction(event -> {
                    String url = "https://www.goodreads.com/book/show/"+getItem();
                    try {
                        new ProcessBuilder("xdg-open", url).start();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
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
        return cell;
    }
}