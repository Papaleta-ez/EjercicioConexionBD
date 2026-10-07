package ni.edu.uam.ejercicioconexionbd.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.ejercicioconexionbd.connection.DatabaseConnection;
import ni.edu.uam.ejercicioconexionbd.model.Libro;
import org.postgresql.shaded.com.ongres.scram.common.StringPreparation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LibroController {
    @FXML
    private TextField txtTitulo;
    @FXML
    private TextField txtAutor;
    @FXML
    private ComboBox<String> cmbCategoria;
    @FXML
    private TextField txtPrecio;
    @FXML
    private TextField txtStock;
    @FXML
    private TableView<Libro> tblLibro;
    @FXML
    private TableColumn<Libro,Integer> colId;
    @FXML
    private TableColumn<Libro,String> colTitulo;
    @FXML
    private TableColumn<Libro,String> colAutor;
    @FXML
    private TableColumn<Libro, String> colCategoria;
    @FXML
    private TableColumn<Libro,Double> colPrecio;
    @FXML
    private TableColumn<Libro,Integer> colStock;

    private final ObservableList<Libro> listaLibros = FXCollections.observableArrayList();

    @FXML
    public void initialize() throws SQLException {
        configurarTabla();
        configurarComboBox();
        cargarLibros();

        // Cada vez que se selecciona una fila del TableView, obtenemos ese objeto.
        tblLibro.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue != null) {
            cargarLibroSeleccionado(newValue);
        }        });
    }

    //Encargado de mapear los datos en los Textfields
    private void cargarLibroSeleccionado(Libro libro) {
        txtTitulo.setText(libro.getTitulo());
        txtAutor.setText(libro.getAutor());
        cmbCategoria.setValue(libro.getCategoria());
        txtPrecio.setText(String.valueOf(libro.getPrecio()));
        txtStock.setText(String.valueOf(libro.getStock()));
    }

    // Definir el tipo de dato que se escribe en cada celda del TableView
    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
    }

    private void configurarComboBox() {
        cmbCategoria.getItems().clear();
        cmbCategoria.getItems().addAll("Tecnología", "Programación", "Ciencia ficción", "Otros");
    }

    @FXML
    private void cargarLibros() throws SQLException {
        listaLibros.clear();
        String sql = "SELECT * FROM libro";
        try(
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery();
                ){
            while (resultSet.next()) {
                Libro libro = new Libro();
                libro.setId(resultSet.getInt("id"));
                libro.setTitulo(resultSet.getString("titulo"));
                libro.setAutor(resultSet.getString("autor"));
                libro.setCategoria(resultSet.getString("categoria"));
                libro.setPrecio(resultSet.getDouble("precio"));
                libro.setStock(resultSet.getInt("stock"));
                listaLibros.add(libro);
            }
            tblLibro.setItems(listaLibros);
        }catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    @FXML
    private void guardarLibro(){
        if(!validarCampos()){
            return;
        }

        // Consulta SQL a ejecutar
        String sql = "INSERT INTO libro (titulo, autor, categoria, precio, stock) VALUES (?, ?, ?, ?, ?)";

        try(
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ){
            statement.setString(1, txtTitulo.getText());
            statement.setString(2, txtAutor.getText());
            statement.setString(3, cmbCategoria.getValue());
            statement.setDouble(4, Double.parseDouble(txtPrecio.getText()));
            statement.setInt(5, Integer.parseInt(txtStock.getText()));
            statement.execute();
            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Registro almacenado",
                    "Libro registrado",
                    "El libro se ha almacenado exitosamente"
            );
        }catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String encabezado, String mensaje){
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private boolean validarCampos(){
        return true;
    }

    @FXML
    private void limpiarRegistro(){
        txtTitulo.clear();
        txtAutor.clear();
        cmbCategoria.setValue(null);
        txtPrecio.clear();
        txtStock.clear();
    }

    @FXML
    private void actualizarRegistro(){
        tblLibro.refresh();
    }

    @FXML
    private void eliminarLibro(){
        Libro libroSeleccionado = tblLibro.getSelectionModel().getSelectedItem();
        if(libroSeleccionado == null){
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Seleccion requerida",
                    "No hay libro seleccionado",
                    "Seleccione un libro del TableView"
            );
            return;

        }
        if(!validarCampos()){
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmacion");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("Esta seguro que desea eliminar el libro seleccionado?");

        if(confirmacion.showAndWait().isEmpty() || confirmacion.getResult() != ButtonType.OK) {
            return;
        }
        //Eliminacion fisica - borrar la fila de la tabla
        String sql= "DELETE FROM libro WHERE id=?";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ){
            statement.setInt(1, libroSeleccionado.getId());
            int filasEliminadas = statement.executeUpdate();
            if(filasEliminadas > 0){
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Registro eliminado",
                        "Eliminacion completa",
                        "El libro fue eliminado exitosamente"

                );
                limpiarRegistro();
                cargarLibros();
            }

        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    @FXML
    private void actualizarLibro(){
        Libro libroSeleccionado = tblLibro.getSelectionModel().getSelectedItem();

        if(libroSeleccionado == null){
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Seleccion requerida",
                    "No hay un libro seleccionado",
                    "Seleccione un libro del tabla"
            );
            return;
        }

        if(!validarCampos()){
            return;
        }
        String sql= "UPDATE libro SET autor=?, categoria=?, precio=? WHERE id=?";

        try(
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ){
            statement.setString(1, txtTitulo.getText());
            statement.setString(2, txtAutor.getText());
            statement.setString(3, cmbCategoria.getValue());
            statement.setDouble(4, Double.parseDouble(txtPrecio.getText()));
            statement.setInt(5, Integer.parseInt(txtStock.getText()));
            statement.setInt(6, libroSeleccionado.getId());

            int filasActualizadas = statement.executeUpdate();
            if(filasActualizadas > 0){
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Registro actualizado",
                        "Actualización completada",
                        "El libro fue actualizado exitosamente"
                );
                limpiarRegistro();
                cargarLibros();
            }
        }catch (SQLException ex){
            ex.printStackTrace();
        }
    }
}
