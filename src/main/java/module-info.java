module com.example.ejercicioconexionbd {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.ejercicioconexionbd to javafx.fxml;
    exports com.example.ejercicioconexionbd;
}