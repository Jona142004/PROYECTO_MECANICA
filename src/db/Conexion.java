package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    // 1. URL CORRECTA (formato JDBC)
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/xe";
    
    // 2. Tus credenciales
    private static final String USER = "AUT_TALLER"; 
    private static final String PASS = "taller123"; 

    public static Connection getConexion() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No has puesto el ojdbc8.jar en la carpeta lib");
            return null;
        } catch (SQLException e) {
            System.err.println("Error Conexión Oracle: " + e.getMessage());
            return null;
        }
    }
}