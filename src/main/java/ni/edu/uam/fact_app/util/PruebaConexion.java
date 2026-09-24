package ni.edu.uam.fact_app.util;
import java.sql.Connection;

public class PruebaConexion {
    public static void main(String[] args){
        try(Connection cn = ConexionDB.getConnection()){
            System.out.println("Conexion exitosa: " + cn.getMetaData().getURL());
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
}
