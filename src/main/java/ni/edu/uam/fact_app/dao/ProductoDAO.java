package ni.edu.uam.fact_app.dao;

import ni.edu.uam.fact_app.model.Categoria;
import ni.edu.uam.fact_app.model.Producto;
import ni.edu.uam.fact_app.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // Guarda un nuevo producto con su categoria relacionada
    // devuelve true si el INSERT se guardo en la base de datos
    public boolean guardar(Producto producto) {
        String sql = """
            INSERT INTO producto (
                codigo,
                nombre,
                categoria_id,
                precio_venta,
                existencia,
                ruta_imagen,
                activo
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        // Abre la conexion y prepara la sentencia
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asigna los valores a los 7 parametros ?
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            // Guarda la llave foranea obteniendo el ID de la categoria
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getRutaImagen());
            ps.setBoolean(7, producto.isActivo());

            // Ejecuta el insert en PostgreSQL
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Consulta todos los productos uniendo la tabla categoria con INNER JOIN
    public List<Producto> listar() {
        List<Producto> lista = new ArrayList<>();
        String sql = """
            SELECT p.*, c.nombre AS categoria_nombre, c.activa AS categoria_activa
            FROM producto p
            INNER JOIN categoria c ON p.categoria_id = c.id
            ORDER BY p.id
            """;

        // Ejecuta la consulta SELECT y obtiene el resultado
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorre cada fila del resultado
            while (rs.next()) {
                // Construye el objeto Categoria con los datos del JOIN
                Categoria c = new Categoria(
                        rs.getInt("categoria_id"),
                        rs.getString("categoria_nombre"),
                        rs.getBoolean("categoria_activa")
                );

                // Construye el objeto Producto asignandole la categoria creada
                Producto p = new Producto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        c,
                        rs.getBigDecimal("precio_venta"),
                        rs.getInt("existencia"),
                        rs.getString("ruta_imagen"),
                        rs.getBoolean("activo"),
                        rs.getString("codigo")
                );

                lista.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Actualiza un producto existente por su id
    public boolean actualizar(Producto producto) {
        String sql = """
            UPDATE producto SET
                codigo = ?,
                nombre = ?,
                categoria_id = ?,
                precio_venta = ?,
                existencia = ?,
                ruta_imagen = ?,
                activo = ?
            WHERE id = ?
            """;

        // Abre la conexion y prepara la sentencia
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asigna los valores a los 8 parametros ?
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            // Llave foranea de la categoria seleccionada
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getRutaImagen());
            ps.setBoolean(7, producto.isActivo());
            ps.setInt(8, producto.getId());

            // Ejecuta el update en PostgreSQL
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Elimina un producto por su id
    public boolean eliminar(Integer id) {
        String sql = "DELETE FROM producto WHERE id = ?";

        // Abre la conexion y prepara la sentencia
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            // Ejecuta el delete en PostgreSQL
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Verifica si ya existe otro producto con el mismo codigo (sin distinguir mayusculas)
    public boolean existeCodigo(String codigo, Integer idExcluir) {
        String sql = "SELECT 1 FROM producto WHERE LOWER(codigo) = LOWER(?) AND id <> ? LIMIT 1";

        // Abre la conexion y prepara la consulta
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            // Al insertar (id null) se usa -1 para no excluir ninguna fila
            ps.setInt(2, idExcluir != null ? idExcluir : -1);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
