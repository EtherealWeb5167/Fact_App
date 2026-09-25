package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.dao.CategoriaDAO;
import ni.edu.uam.fact_app.model.Categoria;

public class CategoriaController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActiva;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, Boolean> colActiva;

    // Instancia del DAO para operaciones en base de datos
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colActiva != null) colActiva.setCellValueFactory(new PropertyValueFactory<>("activa"));

        if (tblCategorias != null) {
            tblCategorias.setItems(categorias);
        }

        if (chkActiva != null) {
            chkActiva.setSelected(true);
        }

        // Carga las categorias reales desde PostgreSQL
        cargarCategorias();
    }

    // Consulta la BD y llena la tabla
    private void cargarCategorias() {
        categorias.clear();
        categorias.addAll(categoriaDAO.listar());
    }

    @FXML
    private void guardar() {
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "El nombre de la categoria es obligatorio.");
            return;
        }

        // Crea el objeto sin id porque PostgreSQL lo genera con SERIAL
        Categoria categoria = new Categoria(null, nombre, chkActiva.isSelected());
        // Guarda en la base de datos
        categoriaDAO.guardar(categoria);

        mensaje(Alert.AlertType.INFORMATION, "Categoria registrada correctamente.");

        // Refresca la tabla y limpia los campos
        cargarCategorias();
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
