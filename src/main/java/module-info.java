module cs.youtrade.cstradeapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.bouncycastle.provider;
    requires kotlin.stdlib;
    requires javasteam;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires com.dlsc.formsfx;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;
    requires org.slf4j;
    requires annotations;

    opens cs.youtrade.cstradeapp to javafx.fxml, com.google.gson;
    exports cs.youtrade.cstradeapp;
    exports cs.youtrade.cstradeapp.storage;
    opens cs.youtrade.cstradeapp.storage to javafx.fxml, com.google.gson;
    opens cs.youtrade.cstradeapp.storage.steamauth to com.google.gson;
}