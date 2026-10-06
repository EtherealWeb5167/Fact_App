package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.dao.CategoriaDAO;
import ni.edu.uam.fact_app.model.Categoria;

import java.sql.SQLException;

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
        try {
            categorias.clear();
            categorias.addAll(categoriaDAO.listar());
        } catch (SQLException e) {
            // Si falla la conexion o la consulta se avisa al usuario
            mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                    "No fue posible cargar las categorias.");
            System.err.println(e.getMessage());
        }
    }

    // Lee y valida el formulario y devuelve la categoria lista para usar,
    // o null si hay algun dato invalido (ya se avisa al usuario con un mensaje)
    private Categoria leerFormulario(Integer id) throws SQLException {
        // trim() quita los espacios, asi un nombre con solo espacios queda vacio
        String nombre = txtNombre.getText().trim();

        // El nombre es obligatorio
        if (nombre.isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "Validacion", "El nombre de la categoria es obligatorio.");
            txtNombre.requestFocus(); // deja el cursor en el campo con error
            return null;
        }

        // Con id null se valida una categoria nueva
        // al editar, el id propio se excluye del chequeo de nombre repetido
        if (categoriaDAO.existeNombre(nombre, id)) {
            mensaje(Alert.AlertType.WARNING, "Nombre duplicado", "Ya existe una categoria con ese nombre.");
            txtNombre.requestFocus();
            return null;
        }

        return new Categoria(id, nombre, chkActiva.isSelected());
    }

    // Registra una categoria nueva validando antes los datos del formulario
    @FXML
    private void guardar() {
        try {
            // Id null porque PostgreSQL lo genera con SERIAL
            Categoria categoria = leerFormulario(null);

            if (categoria == null) {
                return; // los datos del formulario no son validos
            }

            // Guarda en la base de datos y avisa el resultado real del INSERT
            if (categoriaDAO.guardar(categoria)) {
                mensaje(Alert.AlertType.INFORMATION, "Categoria registrada",
                        "La informacion se guardo correctamente.");
            } else {
                mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                        "No fue posible registrar la categoria.");
            }

            // Refresca la tabla y limpia los campos
            cargarCategorias();
            limpiar();

        } catch (SQLException e) {
            // Error de base de datos: no se muestran los detalles tecnicos al usuario
            mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                    "No fue posible registrar la categoria.");
            System.err.println(e.getMessage());
        }
    }

    @FXML
    private void editar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        // Antes de actualizar debe existir una categoria seleccionada
        if (seleccionada == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione una categoria",
                    "Debe seleccionar la categoria que desea actualizar.");
            return;
        }

        try {
            // Conserva el id original para que el UPDATE no cree otra categoria
            Categoria categoria = leerFormulario(seleccionada.getId());

            if (categoria == null) {
                return; // los datos del formulario no son validos
            }

            // Actualiza la categoria en PostgreSQL y avisa el resultado
            if (categoriaDAO.actualizar(categoria)) {
                mensaje(Alert.AlertType.INFORMATION, "Categoria actualizada",
                        "La informacion se actualizo correctamente.");
            } else {
                mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                        "No fue posible actualizar la categoria.");
            }

            // Refresca la tabla y limpia los campos
            cargarCategorias();
            limpiar();

        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                    "No fue posible actualizar la categoria.");
            System.err.println(e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        // Antes de eliminar debe existir una categoria seleccionada
        if (seleccionada == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione una categoria",
                    "Debe seleccionar la categoria que desea eliminar.");
            return;
        }

        // Solicita confirmacion del usuario antes de borrar
        if (!confirmar("¿Desea eliminar la categoria \"" + seleccionada.getNombre() + "\"?")) {
            return;
        }

        try {
            // Una categoria con productos asociados no se puede eliminar
            // (integridad referencial: producto.categoria_id apunta a categoria.id)
            if (categoriaDAO.tieneProductos(seleccionada.getId())) {
                mensaje(Alert.AlertType.WARNING, "Categoria con productos",
                        "No puede eliminar la categoria porque tiene productos asociados.");
                return;
            }

            // Ejecuta el delete y avisa el resultado
            if (categoriaDAO.eliminar(seleccionada.getId())) {
                mensaje(Alert.AlertType.INFORMATION, "Categoria eliminada",
                        "La categoria se elimino correctamente.");
            } else {
                mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                        "No fue posible eliminar la categoria.");
            }

            // Refresca la tabla y limpia los campos
            cargarCategorias();
            limpiar();

        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                    "No fue posible eliminar la categoria.");
            System.err.println(e.getMessage());
        }
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

    // Muestra el mensaje al usuario con un titulo que identifica el tipo de problema
    // tipo: INFORMATION para exito, WARNING para validacion, ERROR para fallas de base de datos
    private void mensaje(Alert.AlertType tipo, String titulo, String texto) {
        Alert alert = new Alert(tipo, texto, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.showAndWait();
    }
}
