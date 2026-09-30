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

            // Al seleccionar una fila de la tabla se cargan sus datos en el formulario
            tblCategorias.getSelectionModel().selectedItemProperty().addListener(
                    (obs, valorAnterior, valorNuevo) -> {
                        if (valorNuevo != null) {
                            txtId.setText(valorNuevo.getId() != null ? String.valueOf(valorNuevo.getId()) : "");
                            txtNombre.setText(valorNuevo.getNombre());
                            chkActiva.setSelected(valorNuevo.isActiva());
                        }
                    });
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

    // Lee y valida el formulario y devuelve la categoria lista para usar,
    // o null si hay algun dato invalido (ya se avisa al usuario con un mensaje)
    private Categoria leerFormulario(Integer id) {
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "El nombre de la categoria es obligatorio.");
            return null;
        }

        // Con id null se valida una categoria nueva
        // al editar, el id propio se excluye del chequeo de nombre repetido
        if (categoriaDAO.existeNombre(nombre, id)) {
            mensaje(Alert.AlertType.WARNING, "Ya existe una categoria con ese nombre.");
            return null;
        }

        return new Categoria(id, nombre, chkActiva.isSelected());
    }

    @FXML
    private void guardar() {
        // Id null porque PostgreSQL lo genera con SERIAL
        Categoria categoria = leerFormulario(null);

        if (categoria == null) {
            return;
        }

        // Guarda en la base de datos
        categoriaDAO.guardar(categoria);
        mensaje(Alert.AlertType.INFORMATION, "Categoria registrada correctamente.");

        // Refresca la tabla y limpia los campos
        cargarCategorias();
        limpiar();
    }

    @FXML
    private void editar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione una categoria de la tabla para editar.");
            return;
        }

        // Conserva el id original de la categoria seleccionada
        Categoria categoria = leerFormulario(seleccionada.getId());

        if (categoria == null) {
            return;
        }

        // Actualiza la categoria en PostgreSQL
        if (categoriaDAO.actualizar(categoria)) {
            mensaje(Alert.AlertType.INFORMATION, "Categoria actualizada correctamente.");
        } else {
            mensaje(Alert.AlertType.ERROR, "No se pudo actualizar la categoria.");
        }

        // Refresca la tabla y limpia los campos
        cargarCategorias();
        limpiar();
    }

    @FXML
    private void eliminar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione una categoria de la tabla para eliminar.");
            return;
        }

        if (!confirmar("¿Desea eliminar la categoria \"" + seleccionada.getNombre() + "\"?")) {
            return;
        }

        boolean eliminado = categoriaDAO.eliminar(seleccionada.getId());

        if (eliminado) {
            mensaje(Alert.AlertType.INFORMATION, "Categoria eliminada correctamente.");
        } else {
            mensaje(Alert.AlertType.ERROR,
                    "No se pudo eliminar. Verifique que la categoria no tenga productos relacionados.");
        }

        cargarCategorias();
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
