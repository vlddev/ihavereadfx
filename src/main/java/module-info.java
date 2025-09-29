module com.vlad.ihaveread {
    requires javafx.controls;
    requires javafx.fxml;

    //requires org.controlsfx.controls;
    //requires com.dlsc.formsfx;
    requires lombok;
    requires java.sql;
    requires org.slf4j;
    requires java.string.similarity;
    requires org.apache.commons.io;
    requires megacmd4j;

    opens com.vlad.ihaveread to javafx.fxml;
    exports com.vlad.ihaveread;

    opens com.vlad.ihaveread.dao to javafx.base;
    exports com.vlad.ihaveread.dao;

    exports com.vlad.ihaveread.db;
}