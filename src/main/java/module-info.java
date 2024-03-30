module com.vlad.ihaveread {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires lombok;
    requires java.sql;
    requires org.slf4j;

    opens com.vlad.ihaveread to javafx.fxml;
    exports com.vlad.ihaveread;
}