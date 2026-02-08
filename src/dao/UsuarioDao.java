package dao;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioDao {
    
    /**
     * Verifica si el usuario y contraseña existen en la BD.
     */
    public boolean login(String usuario, String password) {
        // Asegúrate de tener una tabla llamada USUARIOS con columnas USERNAME y PASSWORD
        String sql = "SELECT USERNAME FROM USUARIOS WHERE USERNAME = ? AND PASSWORD = ?";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            if (con == null) return false; // Si falló la conexión
            
            ps.setString(1, usuario);
            ps.setString(2, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                // Si rs.next() es true, significa que encontró una fila (el usuario existe)
                return rs.next();
            }
            
        } catch (Exception e) {
            System.err.println("Error en login: " + e.getMessage());
            return false;
        }
    }
}