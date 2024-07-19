package com.vlad.ihaveread;

import com.vlad.ihaveread.dao.BookLibFile;
import com.vlad.ihaveread.util.Util;
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
                    T item = getItem();
                    if (item instanceof BookLibFile) {
                        BookLibFile bookLibFile = (BookLibFile)item;
                        try {
                            if (bookLibFile.getLibFile() == null || bookLibFile.getLibFile().isEmpty()) {
                                Path bookDir = Path.of(MainApplication.LIB_ROOT, bookLibFile.getBookDir());
                                if (Files.isDirectory(bookDir)) {
                                    new ProcessBuilder("xdg-open", bookDir.toString()).start();
                                } else {
                                    Util.warningAlert("Warning", "Folder '"+bookLibFile.getBookDir()+"' not exist").show();
                                }
                            } else {
                                String file = MainApplication.LIB_ROOT+bookLibFile.getLibFile();
                                Path filePath = Path.of(file);
                                if (Files.exists(filePath)) {
                                    if (Files.isDirectory(filePath)) {
                                        new ProcessBuilder("xdg-open", file).start();
                                    } else if (file.toLowerCase().endsWith(".epub") ||
                                            file.toLowerCase().endsWith(".fb2") ||
                                            file.toLowerCase().endsWith(".fb2.zip")) {
                                        new ProcessBuilder("foliate", file).start();
                                    } else {
                                        new ProcessBuilder("xdg-open", file).start();
                                    }
                                } else {
                                    Util.warningAlert("Warning", "File '"+file+"' not exist").show();
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    if (item instanceof BookLibFile) {
                        BookLibFile bookLibFile = (BookLibFile)item;
                        if (bookLibFile.getLibFile() == null || bookLibFile.getLibFile().isEmpty()) {
                            hyperlink.setText(bookLibFile.getBookDir());
                        } else {
                            hyperlink.setText(bookLibFile.getLibFile());
                        }
                    } else {
                        hyperlink.setText(item == null ? "" : item.toString());
                    }
                    setGraphic(hyperlink);
                }
            }
        };
    }
}