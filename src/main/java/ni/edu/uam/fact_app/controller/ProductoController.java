package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.dao.CategoriaDAO;
import ni.edu.uam.fact_app.dao.ProductoDAO;
import ni.edu.uam.fact_app.model.Categoria;
import ni.edu.uam.fact_app.model.Producto;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ProductoController {

    @FXML private TextField txtCodigo, txtNombre, txtPrecio, txtExistencia;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProducto;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Categoria> cmbFiltroCategoria;
    @FXML private ComboBox<String> cmbFiltroEstado;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Categoria> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, Boolean> colActivo;

    // Instancias de DAO para consultar y guardar en la base de datos
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private ObservableList<Producto> productos = FXCollections.observableArrayList();
    // lista filtrada que se va a mostrar en el tableview
    private FilteredList<Producto> productosFiltrados = new FilteredList<>(productos);
    private String rutaImagen;

    @FXML
    private void initialize() {
        if (colCodigo != null) colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colCategoria != null) colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        if (colPrecio != null) colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        if (colExistencia != null) colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        if (colActivo != null) colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // filtrar productos por estado (todos activos o inactivos)
        if(cmbFiltroEstado != null){
            cmbFiltroEstado.setItems(FXCollections.observableArrayList("Todos", "Activos", "Inactivos"));
            cmbFiltroEstado.setValue("Todos"); // valor por defecto
            cmbFiltroEstado.valueProperty().addListener((obs, viejo, nuevo) -> aplicarFiltros());
        }

        // Filtrar mientras el usuario va escribiendo
        if(txtBuscar != null){
            txtBuscar.textProperty().addListener((obs, viejo, nuevo) -> aplicarFiltros());
        }

        // Filtrar por categoria
        if(cmbFiltroCategoria != null){
            cmbFiltroCategoria.valueProperty().addListener((obs, viejo, nuevo) -> aplicarFiltros());
        }

        if (tblProductos != null) {
            tblProductos.setItems(productosFiltrados);

            // Al seleccionar una fila de la tabla se cargan sus datos en el formulario
            tblProductos.getSelectionModel().selectedItemProperty().addListener(
                    (obs, valorAnterior, valorNuevo) -> {
                        if (valorNuevo != null) {
                            cargarEnFormulario(valorNuevo);
                        }
                    });
        }

        if (chkActivo != null) {
            chkActivo.setSelected(true);
        }

        // Carga los datos iniciales desde PostgreSQL
        cargarCategorias();
        cargarProductos();
    }

    // Carga las categorias activas en el ComboBox
    private void cargarCategorias() {
        // Consulta las categorias en la base de datos antes de llenar los combos
        ObservableList<Categoria> lista;
        try {
            lista = FXCollections.observableArrayList(categoriaDAO.listar());
        } catch (SQLException e) {
            // Si falla la conexion se avisa al usuario y se dejan los combos vacios
            mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                    "No fue posible cargar las categorias.");
            System.err.println(e.getMessage());
            return;
        }

        if (cmbCategoria != null) {
            cmbCategoria.setItems(lista);
        }

        // el filtro lleva la opcion "Todas" al inicio para poder mostrar todas
        if (cmbFiltroCategoria != null){
            ObservableList<Categoria> paraFiltro = FXCollections.observableArrayList();
            paraFiltro.add(new Categoria(null, "Todas", true)); // id null significa sin filtro
            paraFiltro.addAll(lista);
            cmbFiltroCategoria.setItems(paraFiltro);
            cmbFiltroCategoria.getSelectionModel().selectFirst();
        }
    }

    // Carga todos los productos en la tabla
    private void cargarProductos() {
        try {
            productos.clear();
            productos.addAll(productoDAO.listar());
        } catch (SQLException e) {
            // Si falla la consulta se avisa al usuario
            mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                    "No fue posible cargar los productos.");
            System.err.println(e.getMessage());
        }
    }

    // aplica la busqueda y los filtros sobre la lista filtrada.
    private void aplicarFiltros(){
        String texto = (txtBuscar == null)? "" : txtBuscar.getText().trim().toLowerCase(); // if en una linea
        String estado = (cmbFiltroEstado == null || cmbFiltroEstado.getValue() == null) ? "Todos" : cmbFiltroEstado.getValue();
        Categoria categoria = (cmbFiltroCategoria == null)? null : cmbFiltroCategoria.getValue();

        productosFiltrados.setPredicate(producto -> {
            // busqueda por codigo, nombre o categoria
            if (!texto.isEmpty()){
                boolean coincide = contiene(producto.getCodigo(), texto)
                        || contiene(producto.getNombre(), texto)
                        || (producto.getCategoria() != null && contiene(producto.getCategoria().getNombre(), texto));
                if(!coincide){
                    return false;
                }
            }

            // filtro por estado activos o inactivos
            if ("Activos".equals(estado) && !producto.isActivo()) {
                return false;
            }
            if ("Inactivos".equals(estado) && producto.isActivo()) {
                return false;
            }

            // filtro por la categoria elegida en el combo
            // la opcion "Todas" tiene id null, o sea que no filtra
            if (categoria != null && categoria.getId() != null) {
                if (producto.getCategoria() == null
                        || !categoria.getId().equals(producto.getCategoria().getId())) {
                    return false;
                }
            }

            // si paso los tres filtros se muestra en la tabla
            return true;
        });
    }

    // compara sin importar mayusculas y evita error si el valor es nulo
    private boolean contiene(String valor, String texto) {
        return valor != null && valor.toLowerCase().contains(texto);
    }

    // Rellena el formulario con los datos del producto seleccionado en la tabla
    private void cargarEnFormulario(Producto producto) {
        txtCodigo.setText(producto.getCodigo());
        txtNombre.setText(producto.getNombre());
        txtPrecio.setText(producto.getPrecioVenta() != null ? producto.getPrecioVenta().toPlainString() : "");
        txtExistencia.setText(String.valueOf(producto.getExistencia()));
        chkActivo.setSelected(producto.isActivo());

        // Selecciona la categoria del producto en el ComboBox
        if (producto.getCategoria() != null && cmbCategoria != null) {
            for (Categoria categoria : cmbCategoria.getItems()) {
                if (categoria.getId() != null && categoria.getId().equals(producto.getCategoria().getId())) {
                    cmbCategoria.getSelectionModel().select(categoria);
                    break;
                }
            }
        }

        // Muestra la imagen guardada del producto
        rutaImagen = producto.getRutaImagen();
        if (imgProducto != null) {
            if (rutaImagen != null && !rutaImagen.isBlank()) {
                try {
                    imgProducto.setImage(new Image(rutaImagen));
                } catch (IllegalArgumentException e) {
                    imgProducto.setImage(null);
                }
            } else {
                imgProducto.setImage(null);
            }
        }
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = chooser.showOpenDialog(txtCodigo.getScene().getWindow());
        if (archivo != null) {
            rutaImagen = archivo.toURI().toString();
            if (imgProducto != null) {
                imgProducto.setImage(new Image(rutaImagen));
            }
        }
    }

    // Lee y valida el formulario y construye el producto listo para usar.
    // Si algun dato es invalido lanza IllegalArgumentException con el mensaje de error,
    // y ese mensaje lo muestra al usuario el metodo que llamo (guardar o editar)
    private Producto obtenerProductoFormulario(Integer id) {
        // trim() quita los espacios: un campo con solo espacios queda vacio
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();

        // Valida los campos obligatorios uno por uno y deja el cursor en el que fallo
        if (codigo.isEmpty()) {
            txtCodigo.requestFocus();
            throw new IllegalArgumentException("El codigo es obligatorio.");
        }

        if (nombre.isEmpty()) {
            txtNombre.requestFocus();
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        // El producto necesita una categoria porque categoria_id es llave foranea
        Categoria categoria = cmbCategoria.getSelectionModel().getSelectedItem();

        if (categoria == null) {
            cmbCategoria.requestFocus();
            throw new IllegalArgumentException("Debe seleccionar una categoria.");
        }

        if (txtPrecio.getText().isBlank()) {
            txtPrecio.requestFocus();
            throw new IllegalArgumentException("El precio es obligatorio.");
        }

        // El TextField devuelve texto, hay que convertirlo a BigDecimal
        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            // El usuario escribio algo que no es un numero
            txtPrecio.requestFocus();
            throw new IllegalArgumentException("El precio debe ser un valor numerico.");
        }

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            txtPrecio.requestFocus();
            throw new IllegalArgumentException("El precio de venta debe ser mayor que cero.");
        }

        if (txtExistencia.getText().isBlank()) {
            txtExistencia.requestFocus();
            throw new IllegalArgumentException("La existencia es obligatoria.");
        }

        // La existencia solo acepta numeros enteros
        int existencia;
        try {
            existencia = Integer.parseInt(txtExistencia.getText().trim());
        } catch (NumberFormatException e) {
            txtExistencia.requestFocus();
            throw new IllegalArgumentException("La existencia debe ser un numero entero.");
        }

        if (existencia < 0) {
            txtExistencia.requestFocus();
            throw new IllegalArgumentException("La existencia no puede ser negativa.");
        }

        // Devuelve el producto con los datos ya validados
        return new Producto(
                id,
                nombre,
                categoria,
                precio,
                existencia,
                rutaImagen,
                chkActivo.isSelected(),
                codigo
        );
    }

    @FXML
    private void guardar() {
        try {
            // Id null porque PostgreSQL lo genera con SERIAL
            Producto producto = obtenerProductoFormulario(null);

            // Antes del insert se verifica que el codigo no este repetido
            if (productoDAO.existeCodigo(producto.getCodigo(), null)) {
                mensaje(Alert.AlertType.WARNING, "Codigo duplicado",
                        "Ya existe un producto con ese codigo.");
                return;
            }

            // Guarda el producto en PostgreSQL y avisa el resultado del INSERT
            if (productoDAO.guardar(producto)) {
                mensaje(Alert.AlertType.INFORMATION, "Producto registrado",
                        "La informacion fue almacenada correctamente.");
            } else {
                mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                        "No fue posible registrar el producto.");
            }

            // Refresca la tabla y limpia el formulario
            cargarProductos();
            limpiar();

        } catch (IllegalArgumentException e) {
            // Error de validacion: el mensaje ya indica que campo corregir
            mensaje(Alert.AlertType.WARNING, "Validacion", e.getMessage());
        } catch (SQLException e) {
            // Error de base de datos: al usuario se le da un mensaje general
            mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                    "No fue posible registrar el producto.");
            System.err.println(e.getMessage());
        }
    }

    @FXML
    private void editar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();

        // Antes de actualizar debe existir un producto seleccionado
        if (seleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto",
                    "Debe seleccionar el producto que desea actualizar.");
            return;
        }

        try {
            // Conserva el id original para que el UPDATE no cree un producto nuevo
            Producto producto = obtenerProductoFormulario(seleccionado.getId());

            // El codigo no puede pertenecer a otro producto distinto
            if (productoDAO.existeCodigo(producto.getCodigo(), producto.getId())) {
                mensaje(Alert.AlertType.WARNING, "Codigo duplicado",
                        "Ya existe otro producto con ese codigo.");
                return;
            }

            // Actualiza el producto en PostgreSQL y avisa el resultado
            if (productoDAO.actualizar(producto)) {
                mensaje(Alert.AlertType.INFORMATION, "Producto actualizado",
                        "La informacion se actualizo correctamente.");
            } else {
                mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                        "No fue posible actualizar el producto.");
            }

            cargarProductos();
            limpiar();

        } catch (IllegalArgumentException e) {
            mensaje(Alert.AlertType.WARNING, "Validacion", e.getMessage());
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                    "No fue posible actualizar el producto.");
            System.err.println(e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();

        // Antes de eliminar debe existir un producto seleccionado
        if (seleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto",
                    "Debe seleccionar el producto que desea eliminar.");
            return;
        }

        // Solicita confirmacion del usuario antes de borrar
        if (!confirmar("¿Desea eliminar el producto \"" + seleccionado.getNombre() + "\"?")) {
            return;
        }

        try {
            // Ejecuta el delete y avisa el resultado
            if (productoDAO.eliminar(seleccionado.getId())) {
                mensaje(Alert.AlertType.INFORMATION, "Producto eliminado",
                        "El producto se elimino correctamente.");
            } else {
                mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                        "No fue posible eliminar el producto.");
            }

            cargarProductos();
            limpiar();

        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error de base de datos",
                    "No fue posible eliminar el producto.");
            System.err.println(e.getMessage());
        }
    }

    // Ventana de confirmacion: devuelve true solo si el usuario acepta
    private boolean confirmar(String texto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, texto, ButtonType.YES, ButtonType.NO);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    @FXML
    private void cerrar() {
        ((Stage) txtCodigo.getScene().getWindow()).close();
    }

    @FXML
    private void limpiar() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();
        if (cmbCategoria != null) cmbCategoria.getSelectionModel().clearSelection();
        if (chkActivo != null) chkActivo.setSelected(true);
        if (imgProducto != null) imgProducto.setImage(null);
        rutaImagen = null;
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
