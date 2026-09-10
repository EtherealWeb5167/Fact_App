package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.model.Cargo;

public class CargoController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;

    @FXML private TableView<Cargo> tblCargos;
    @FXML private TableColumn<Cargo, Integer> colId;
    @FXML private TableColumn<Cargo, String> colNombre;
    @FXML private TableColumn<Cargo, String> colDescripcion;

    private final ObservableList<Cargo> cargos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colDescripcion != null) colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        cargos.add(new Cargo(1, "Gerente", "Gerente general de tienda"));
        cargos.add(new Cargo(2, "Cajero", "Cajero de punto de venta"));
        cargos.add(new Cargo(3, "Vendedor", "Atención a clientes y ventas"));

        if (tblCargos != null) {
            tblCargos.setItems(cargos);
        }
    }

    @FXML
    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        if (nombre.isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "El nombre del cargo es obligatorio.");
            return;
        }

        Integer id;
        try {
            if (!txtId.getText().isBlank()) {
                id = Integer.parseInt(txtId.getText().trim());
            } else {
                id = cargos.size() + 1;
            }
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "El id debe ser un numero entero valido.");
            return;
        }

        Cargo cargo = new Cargo(id, nombre, descripcion);
        cargos.add(cargo);
        mensaje(Alert.AlertType.INFORMATION, "Cargo registrado correctamente.");
        limpiar();
    }

    @FXML
    private void limpiar() {
        txtId.clear();
        txtNombre.clear();
        txtDescripcion.clear();
    }

    @FXML
    private void cerrar() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}
