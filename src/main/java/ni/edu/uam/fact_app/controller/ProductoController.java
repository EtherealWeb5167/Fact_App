package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.model.Categoria;
import ni.edu.uam.fact_app.model.Producto;

import java.io.File;
import java.math.BigDecimal;

public class ProductoController {

    @FXML private TextField txtCodigo, txtNombre, txtPrecio, txtExistencia;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProducto;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Categoria> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, Boolean> colActivo;

    private ObservableList<Producto> productos = FXCollections.observableArrayList();
    private String rutaImagen;

    @FXML
    private void initialize() {
        if (cmbCategoria != null) {
            cmbCategoria.setItems(FXCollections.observableArrayList(
                    new Categoria(1, "Alimentos", true),
                    new Categoria(2, "Bebidas", true),
                    new Categoria(3, "Limpieza", true)
            ));
        }

        if (colCodigo != null) colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colCategoria != null) colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        if (colPrecio != null) colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        if (colExistencia != null) colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        if (colActivo != null) colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        if (tblProductos != null) {
            tblProductos.setItems(productos);
        }

        if (chkActivo != null) {
            chkActivo.setSelected(true);
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

    @FXML
    private void guardar() {
        if (txtCodigo.getText().isBlank() || txtNombre.getText().isBlank()
                || txtPrecio.getText().isBlank() || txtExistencia.getText().isBlank()
                || cmbCategoria.getValue() == null) {
            mensaje(Alert.AlertType.WARNING, "Complete los campos obligatorios.");
            return;
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int existencia = Integer.parseInt(txtExistencia.getText().trim());

            if (precio.compareTo(BigDecimal.ZERO) <= 0 || existencia < 0) {
                mensaje(Alert.AlertType.WARNING, "Precio mayor que cero y existencia no negativa.");
                return;
            }

            productos.add(new Producto(
                    null,
                    txtNombre.getText().trim(),
                    cmbCategoria.getValue(),
                    precio,
                    existencia,
                    rutaImagen,
                    chkActivo.isSelected(),
                    txtCodigo.getText().trim()
            ));

            mensaje(Alert.AlertType.INFORMATION, "Producto agregado correctamente.");
            limpiar();
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "Precio o existencia no validos.");
        }
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
