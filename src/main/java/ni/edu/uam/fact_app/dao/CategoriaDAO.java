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
    public void guardar(Categoria categoria) {
        String sql = "INSERT INTO categoria (nombre, activa) VALUES (?, ?)";

        // Abre la conexion y prepara la consulta SQL
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asigna los valores a los parametros ?
            ps.setString(1, categoria.getNombre());
            ps.setBoolean(2, categoria.isActiva());
            // Ejecuta el insert en PostgreSQL
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Consulta y devuelve todas las categorias registradas
    public List<Categoria> listar() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, activa FROM categoria ORDER BY id";

        // Ejecuta la consulta SELECT y obtiene los resultados
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Recorre cada fila del resultado de la base de datos
            while (rs.next()) {
                // Crea el objeto Categoria con los datos de la fila actual
                Categoria c = new Categoria(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getBoolean("activa")
                );
                lista.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Actualiza el nombre y el estado de una categoria existente
    public boolean actualizar(Categoria categoria) {
        String sql = "UPDATE categoria SET nombre = ?, activa = ? WHERE id = ?";

        // Abre la conexion y prepara la sentencia
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setBoolean(2, categoria.isActiva());
            ps.setInt(3, categoria.getId());

            // Ejecuta el update en PostgreSQL
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Elimina una categoria por su id
    public boolean eliminar(Integer id) {
        String sql = "DELETE FROM categoria WHERE id = ?";

        // Abre la conexion y prepara la sentencia
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            // Ejecuta el delete en PostgreSQL
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Falla si la categoria tiene productos relacionados (llave foranea)
            e.printStackTrace();
            return false;
        }
    }

    // Verifica si ya existe otra categoria con el mismo nombre (sin distinguir mayusculas)
    public boolean existeNombre(String nombre, Integer idExcluir) {
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

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
