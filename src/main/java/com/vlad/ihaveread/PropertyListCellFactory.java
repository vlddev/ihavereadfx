package com.vlad.ihaveread;

import javafx.beans.NamedArg;
import javafx.scene.control.*;

import java.lang.reflect.Field;

public class PropertyListCellFactory<T> extends ListCell<T> {

    private final String property;

    public PropertyListCellFactory(@NamedArg("property") String property) {
        this.property = property;
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
        } else {
            Field field = null;
            try {
                field = item.getClass().getDeclaredField(this.property);
                field.setAccessible(true);
                Object value = field.get(item);
                setText(value.toString());
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}