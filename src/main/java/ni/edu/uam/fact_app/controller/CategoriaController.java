package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.model.Categoria;

public class CategoriaController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActiva;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, Boolean> colActiva;

    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colActiva != null) colActiva.setCellValueFactory(new PropertyValueFactory<>("activa"));

        categorias.add(new Categoria(1, "Alimentos", true));
        categorias.add(new Categoria(2, "Bebidas", true));
        categorias.add(new Categoria(3, "Limpieza", true));

        if (tblCategorias != null) {
            tblCategorias.setItems(categorias);
        }

        chkActiva.setSelected(true);
    }

    @FXML
    private void guardar() {
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "El nombre de la categoria es obligatorio.");
            return;
        }

        Integer id;
        try {
            if (!txtId.getText().isBlank()) {
                id = Integer.parseInt(txtId.getText().trim());
            } else {
                id = categorias.size() + 1;
            }
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "El id debe ser un numero entero valido.");
            return;
        }

        Categoria categoria = new Categoria(id, nombre, chkActiva.isSelected());
        categorias.add(categoria);
        mensaje(Alert.AlertType.INFORMATION, "Categoria registrada correctamente.");
        limpiar();
    }

    @FXML
    private void limpiar() {
        txtId.clear();
        txtNombre.clear();
        chkActiva.setSelected(true);
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
