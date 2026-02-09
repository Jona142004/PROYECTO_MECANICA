package dao;

import db.Conexion;
import model.Sesion;
import model.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // --- LOGIN (IGUAL QUE ANTES) ---
    public Sesion login(String usuario, String clave) {
        Sesion sesion = null;
        String sql = "SELECT u.usu_id, u.usu_usuario, u.usu_tipo_permiso, e.emp_nombre, e.emp_apellido " +
                     "FROM AUT_USUARIOS u " +
                     "JOIN AUT_EMPLEADOS e ON e.emp_id = u.AUT_EMPLEADOS_emp_id " +
                     "WHERE u.usu_usuario = ? AND u.usu_contrasenia = ? AND u.usu_estado = 'A'";
        try (Connection c = Conexion.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, clave);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sesion = new Sesion(rs.getInt("usu_id"), rs.getString("usu_usuario"), 
                                        rs.getString("emp_nombre") + " " + rs.getString("emp_apellido"), 
                                        rs.getString("usu_tipo_permiso"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return sesion;
    }

    // ==========================================
    // MÉTODOS CRUD
    // ==========================================

    // 1. REGISTRAR (AHORA USA LA LÓGICA DE TU INSERT SQL)
    public boolean registrar(Usuario u, String cedulaEmpleado) {
        // Usamos seq_usuarios.NEXTVAL para el ID del usuario
        // Y una subconsulta para buscar el ID del empleado por su CÉDULA
        String sql = "INSERT INTO AUT_USUARIOS (usu_id, usu_usuario, usu_contrasenia, usu_tipo_permiso, usu_estado, AUT_EMPLEADOS_emp_id) " +
                     "VALUES (seq_usuarios.NEXTVAL, ?, ?, ?, 'A', " +
                     "(SELECT emp_id FROM AUT_EMPLEADOS WHERE emp_cedula = ? AND emp_rol='R'))";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, u.getUsuario());
            ps.setString(2, u.getClave());
            ps.setString(3, u.getRol());      // 'A' o 'E'
            ps.setString(4, cedulaEmpleado);  // Pasamos la cédula para la subconsulta
            
            int filas = ps.executeUpdate();
            return filas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error registrar: " + e.getMessage());
            // Si el error es de integridad (NULL), significa que no encontró la cédula o no es rol 'R'
            return false;
        }
    }

    // 2. LISTAR
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.usu_id, u.usu_usuario, u.usu_tipo_permiso, u.usu_estado, " +
                     "e.emp_nombre, e.emp_apellido, e.emp_cedula " + // Traemos cédula también
                     "FROM AUT_USUARIOS u " +
                     "JOIN AUT_EMPLEADOS e ON u.AUT_EMPLEADOS_emp_id = e.emp_id " +
                     "WHERE u.usu_estado = 'A' " +
                     "ORDER BY u.usu_usuario";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("usu_id"));
                u.setUsuario(rs.getString("usu_usuario"));
                u.setRol(rs.getString("usu_tipo_permiso"));
                u.setEstado(rs.getString("usu_estado"));
                // Concatenamos Cédula - Nombre para que se vea claro en la tabla
                u.setNombreEmpleado(rs.getString("emp_cedula") + " - " + rs.getString("emp_nombre") + " " + rs.getString("emp_apellido"));
                lista.add(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    // 3. ELIMINAR
    public boolean eliminar(int id) {
        String sql = "UPDATE AUT_USUARIOS SET usu_estado = 'I' WHERE usu_id = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}