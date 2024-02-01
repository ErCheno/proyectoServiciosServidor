module com.example.servidorservicios {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.net;
    requires jakarta.mail;


    opens com.example.servidorservicios to javafx.fxml;
    exports com.example.servidorservicios;
}