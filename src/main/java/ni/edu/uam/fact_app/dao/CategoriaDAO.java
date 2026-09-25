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
}
