package com.vlad.ihaveread;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.io.IOException;

public class GoodreadsLinkCell<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {

    private static final String GOODREADS_SHOW_LINK = "https://www.goodreads.com/book/show/";
    private static final String GOODREADS_SEARCH_LINK = "https://www.goodreads.com/search?q=";

    @Override
    public TableCell<S, T> call(TableColumn<S, T> arg) {
        return new TableCell<>() {

            private final Hyperlink hyperlink = new Hyperlink();

            {
                hyperlink.setOnAction(event -> {
                    String strItem = (getItem() == null ? "" : getItem().toString());
                    String url = GOODREADS_SHOW_LINK+strItem;
                    if (strItem.startsWith("search:")) {
                        strItem = strItem.substring(7).replace(" ", "+");
                        url = GOODREADS_SEARCH_LINK+strItem;
                    } else if (strItem.startsWith("alt:")) {
                        url = GOODREADS_SHOW_LINK+strItem.substring(4);
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