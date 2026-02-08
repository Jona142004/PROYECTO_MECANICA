package dao;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioDAO {

    public static class SesionInfo {
        public boolean valido = false;
        public String nombreCompleto;
        public String rol; // 'A' o 'E'
        public String usuario;
    }

    public SesionInfo login(String usuario, String clave) {
        SesionInfo info = new SesionInfo();

        String sql =
            "SELECT " +
            "  u.usu_usuario        AS usuario, " +
            "  u.usu_tipo_permiso   AS rol, " +
            "  e.emp_nombre         AS nombre, " +
            "  e.emp_apellido       AS apellido " +
            "FROM AUT_USUARIOS u " +
            "JOIN AUT_EMPLEADOS e ON e.emp_id = u.AUT_EMPLEADOS_emp_id " +
            "WHERE u.usu_usuario = ? " +
            "  AND u.usu_contrasenia = ? " +
            "  AND u.usu_estado = 'A'";

        try (Connection c = Conexion.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, clave);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    info.valido = true;
                    info.usuario = rs.getString("usuario");
                    info.rol = rs.getString("rol"); // 'A' admin, 'E' empleado
                    info.nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                }
            }

        } catch (Exception e) {
            System.err.println("Error Login: " + e.getMessage());
        }

        return info;
    }
}
