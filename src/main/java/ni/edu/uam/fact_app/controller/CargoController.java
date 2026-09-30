package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.model.Cargo;

import java.util.Objects;

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

            // Al seleccionar una fila de la tabla se cargan sus datos en el formulario
            tblCargos.getSelectionModel().selectedItemProperty().addListener(
                    (obs, valorAnterior, valorNuevo) -> {
                        if (valorNuevo != null) {
                            txtId.setText(valorNuevo.getId() != null ? String.valueOf(valorNuevo.getId()) : "");
                            txtNombre.setText(valorNuevo.getNombre());
                            txtDescripcion.setText(valorNuevo.getDescripcion());
                        }
                    });
        }
    }

    // Valida que el nombre no este vacio ni repetido en la tabla.
    // "excluir" es el cargo que se esta editando (null al agregar uno nuevo)
    private boolean validarNombre(String nombre, Cargo excluir) {
        if (nombre.isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "El nombre del cargo es obligatorio.");
            return false;
        }

        boolean repetido = cargos.stream()
                .anyMatch(c -> c != excluir && nombre.equalsIgnoreCase(c.getNombre()));
        if (repetido) {
            mensaje(Alert.AlertType.WARNING, "Ya existe un cargo con ese nombre.");
            return false;
        }
        return true;
    }

    @FXML
    private void guardar() {
        String nombre = txtNombre.getText().trim();

        // Detiene el registro si el nombre esta vacio o repetido
        if (!validarNombre(nombre, null)) {
            return;
        }

        Integer id;
        try {
            if (!txtId.getText().isBlank()) {
                id = Integer.parseInt(txtId.getText().trim());
            } else {
                // Id maximo + 1 para evitar duplicados tras una eliminacion
                id = cargos.stream()
                        .map(Cargo::getId)
                        .filter(Objects::nonNull)
                        .max(Integer::compareTo)
                        .orElse(0) + 1;
            }
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "El id debe ser un numero entero valido.");
            return;
        }

        Cargo cargo = new Cargo(id, nombre, txtDescripcion.getText().trim());
        cargos.add(cargo);
        mensaje(Alert.AlertType.INFORMATION, "Cargo registrado correctamente.");
        limpiar();
    }

    @FXML
    private void editar() {
        Cargo seleccionado = tblCargos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un cargo de la tabla para editar.");
            return;
        }

        String nombre = txtNombre.getText().trim();

        // El propio cargo seleccionado se excluye del chequeo de nombre repetido
        if (!validarNombre(nombre, seleccionado)) {
            return;
        }

        // Conserva el id original y actualiza nombre y descripcion
        seleccionado.setNombre(nombre);
        seleccionado.setDescripcion(txtDescripcion.getText().trim());

        if (tblCargos != null) {
            tblCargos.refresh();
        }

        mensaje(Alert.AlertType.INFORMATION, "Cargo actualizado correctamente.");
        limpiar();
    }

    @FXML
    private void eliminar() {
        Cargo seleccionado = tblCargos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un cargo de la tabla para eliminar.");
            return;
        }

        if (!confirmar("¿Desea eliminar el cargo \"" + seleccionado.getNombre() + "\"?")) {
            return;
        }

        cargos.remove(seleccionado);

        if (tblCargos != null) {
            tblCargos.refresh();
        }

        mensaje(Alert.AlertType.INFORMATION, "Cargo eliminado correctamente.");
        limpiar();
    }

    // Ventana de confirmacion: devuelve true solo si el usuario acepta
    private boolean confirmar(String texto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, texto, ButtonType.YES, ButtonType.NO);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
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
