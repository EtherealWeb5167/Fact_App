package ni.edu.uam.fact_app.dao;

import ni.edu.uam.fact_app.model.Categoria;
import ni.edu.uam.fact_app.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    // Guarda una nueva categoria en la base de datos
    // devuelve true si el INSERT se guardo en la base de datos
    public boolean guardar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (nombre, activa) VALUES (?, ?)";

        // Abre la conexion y prepara la consulta SQL
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asigna los valores a los parametros ?
            ps.setString(1, categoria.getNombre());
            ps.setBoolean(2, categoria.isActiva());

            // Ejecuta el insert en PostgreSQL
            return ps.executeUpdate() > 0;
        }
    }

    // Consulta todas las categorias ordenadas por id
    public List<Categoria> listar() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, activa FROM categoria ORDER BY id";

        // Abre la conexion, prepara la consulta y obtiene el resultado
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorre cada fila del resultado
            while (rs.next()) {
                // Construye el objeto Categoria con los datos de la fila
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre"), rs.getBoolean("activa")));
            }
        }
        return lista;
    }

    // Actualiza una categoria existente por su id
    public boolean actualizar(Categoria categoria) throws SQLException {
        String sql = "UPDATE categoria SET nombre = ?, activa = ? WHERE id = ?";

        // Abre la conexion y prepara la sentencia
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asigna los valores a los 3 parametros ?
            ps.setString(1, categoria.getNombre());
            ps.setBoolean(2, categoria.isActiva());
            ps.setInt(3, categoria.getId());

            // Ejecuta el update en PostgreSQL
            return ps.executeUpdate() > 0;
        }
    }

    // Elimina una categoria por su id
    public boolean eliminar(Integer id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";

        // Abre la conexion y prepara la sentencia
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            // Ejecuta el delete en PostgreSQL
            return ps.executeUpdate() > 0;
        }
    }

    // Verifica si ya existe otra categoria con el mismo nombre (sin distinguir mayusculas)
    public boolean existeNombre(String nombre, Integer idExcluir) throws SQLException {
        String sql = "SELECT 1 FROM categoria WHERE LOWER(nombre) = LOWER(?) AND id <> ? LIMIT 1";

        // Abre la conexion y prepara la consulta
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            // Al insertar (id null) se usa -1 para no excluir ninguna fila
            ps.setInt(2, idExcluir != null ? idExcluir : -1);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Verifica si la categoria tiene productos relacionados antes de eliminarla
    public boolean tieneProductos(int categoriaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto WHERE categoria_id = ?";

        // Abre la conexion y prepara la consulta
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, categoriaId);

            // Cuenta los productos de la categoria. hay relacionados si el total es mayor a cero
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
