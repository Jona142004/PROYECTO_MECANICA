package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    // DATOS DE TU BASE DE DATOS ORACLE
    // Nota: Usamos "xepdb1" porque es el estándar moderno que vimos que tenías.
    // Si te da error, prueba cambiando "/xepdb1" por ":xe"
    private static final String URL = "conexion_taller\tAUT_TALLER@//localhost:1521/xe";
    
    
    private static final String USER = "AUT_TALLER";  // O el usuario que hayas creado
    private static final String PASS = "taller1234"; 

    public static Connection getConexion() {
        
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            System.err.println("Falta el ojdbc8.jar en la carpeta lib");
            return null;
        } catch (SQLException e) {
            System.err.println("Error Conexión Oracle: " + e.getMessage());
            return null;
        }
    }
}