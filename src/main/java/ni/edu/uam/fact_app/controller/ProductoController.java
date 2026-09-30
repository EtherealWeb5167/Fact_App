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
        ObservableList<Categoria> lista = FXCollections.observableArrayList(categoriaDAO.listar());

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
        productos.clear();
        productos.addAll(productoDAO.listar());
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

    // Lee y valida el formulario y devuelve el producto listo para usar,
    // o null si hay algun dato invalido (ya se avisa al usuario con un mensaje)
    private Producto leerFormulario(Integer id) {
        if (txtCodigo.getText().isBlank() || txtNombre.getText().isBlank()
                || txtPrecio.getText().isBlank() || txtExistencia.getText().isBlank()
                || cmbCategoria.getValue() == null) {
            mensaje(Alert.AlertType.WARNING, "Complete los campos obligatorios.");
            return null;
        }

        BigDecimal precio;
        int existencia;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
            existencia = Integer.parseInt(txtExistencia.getText().trim());
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "Precio o existencia no validos.");
            return null;
        }

        if (precio.compareTo(BigDecimal.ZERO) <= 0 || existencia < 0) {
            mensaje(Alert.AlertType.WARNING, "Precio mayor que cero y existencia no negativa.");
            return null;
        }

        // Con id null se valida un producto nuevo;
        // al editar, el id propio se excluye del chequeo de codigo repetido
        if (productoDAO.existeCodigo(txtCodigo.getText().trim(), id)) {
            mensaje(Alert.AlertType.WARNING, "Ya existe un producto con ese codigo.");
            return null;
        }

        return new Producto(
                id,
                txtNombre.getText().trim(),
                cmbCategoria.getValue(),
                precio,
                existencia,
                rutaImagen,
                chkActivo.isSelected(),
                txtCodigo.getText().trim()
        );
    }

    @FXML
    private void guardar() {
        // Id null porque PostgreSQL lo genera con SERIAL
        Producto producto = leerFormulario(null);

        if (producto == null) {
            return;
        }

        // Guarda el producto en PostgreSQL
        if (productoDAO.guardar(producto)) {
            mensaje(Alert.AlertType.INFORMATION, "Producto agregado correctamente.");
        } else {
            mensaje(Alert.AlertType.ERROR, "No se pudo guardar el producto. Verifique la conexion a la base de datos.");
        }

        // Refresca la tabla y limpia el formulario
        cargarProductos();
        limpiar();
    }

    @FXML
    private void editar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto de la tabla para editar.");
            return;
        }

        // Conserva el id original del producto seleccionado
        Producto producto = leerFormulario(seleccionado.getId());

        if (producto == null) {
            return;
        }

        // Actualiza el producto en PostgreSQL
        if (productoDAO.actualizar(producto)) {
            mensaje(Alert.AlertType.INFORMATION, "Producto actualizado correctamente.");
        } else {
            mensaje(Alert.AlertType.ERROR, "No se pudo actualizar el producto.");
        }

        cargarProductos();
        limpiar();
    }

    @FXML
    private void eliminar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto de la tabla para eliminar.");
            return;
        }

        if (!confirmar("¿Desea eliminar el producto \"" + seleccionado.getNombre() + "\"?")) {
            return;
        }

        boolean eliminado = productoDAO.eliminar(seleccionado.getId());

        if (eliminado) {
            mensaje(Alert.AlertType.INFORMATION, "Producto eliminado correctamente.");
        } else {
            mensaje(Alert.AlertType.ERROR, "No se pudo eliminar el producto.");
        }

        cargarProductos();
        limpiar();
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

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}
