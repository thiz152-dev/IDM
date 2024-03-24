module com.example.idm {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.idm to javafx.fxml;
    opens com.example.idm.fileService to javafx.base;
    exports com.example.idm;
    exports com.example.idm.fileService;
}