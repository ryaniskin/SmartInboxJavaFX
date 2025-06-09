module com.smartinbox.smartinboxjavafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.google.api.services.gmail;
    requires org.apache.commons.codec;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.client.auth;
    requires google.api.client;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires org.apache.pdfbox;
    requires jdk.httpserver;

    opens com.smartinbox.smartinboxjavafx to javafx.fxml;
    exports com.smartinbox.smartinboxjavafx;
}