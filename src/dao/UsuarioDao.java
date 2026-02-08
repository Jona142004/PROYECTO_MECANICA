package dao;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioDAO {
    
    public static class SesionInfo {
        public boolean valido = false;
        public String nombreCompleto;
        public String rol; 
    }

    public SesionInfo login(String cedula, String clave) {
        SesionInfo info = new SesionInfo();

        // --- ADAPTACIÓN A TU BASE DE DATOS ---
        // 1. Buscamos por e.emp_cedula (Tabla Empleados) en vez de usuario.
        // 2. Usamos los nombres reales de tus columnas: emp_nombre, usu_contrasenia
        String sql = "SELECT e.emp_nombre, e.emp_apellido, u.usu_tipo_permiso " +
                     "FROM AUT_USUARIOS u " +
                     "JOIN AUT_EMPLEADOS e ON u.AUT_EMPLEADOS_emp_id = e.emp_id " +
                     "WHERE e.emp_cedula = ? AND u.usu_contrasenia = ?";
        
        Connection con = Conexion.getConexion();
        if (con == null) {
            return info; // Retorna falso si no hay conexión
        }

        try (Connection c = con;
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, cedula); // El primer ? será la Cédula
            ps.setString(2, clave);  // El segundo ? será la Contraseña
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    info.valido = true;
                    // Concatenamos nombre y apellido para el saludo
                    info.nombreCompleto = rs.getString("emp_nombre") + " " + rs.getString("emp_apellido");
                    info.rol = rs.getString("usu_tipo_permiso");
                }
            }
        } catch (Exception e) {
            System.err.println("Error Login: " + e.getMessage());
        }
        return info;
    }
}