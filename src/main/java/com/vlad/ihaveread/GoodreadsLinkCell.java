package com.vlad.ihaveread;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.io.IOException;

public class GoodreadsLinkCell<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {

    @Override
    public TableCell<S, T> call(TableColumn<S, T> arg) {
        return new TableCell<>() {

            private final Hyperlink hyperlink = new Hyperlink();

            {
                hyperlink.setOnAction(event -> {
                    String strItem = (getItem() == null ? "" : getItem().toString());
                    String url = "https://www.goodreads.com/book/show/"+strItem;
                    if (strItem.startsWith("search:")) {
                        strItem = strItem.substring(7).replace(" ", "+");
                        url = "https://www.goodreads.com/search?q="+strItem;
                    }
                    try {
                        new ProcessBuilder("xdg-open", url).start();
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
                    String linkText = (item == null ? "" : item.toString());
                    if (linkText.startsWith("search:")) {
                        linkText = "search";
                    }
                    hyperlink.setText(linkText);
                    setGraphic(hyperlink);
                }
            }
        };
    }
}