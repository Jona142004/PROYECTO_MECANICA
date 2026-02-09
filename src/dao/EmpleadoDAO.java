package dao;

import db.Conexion;
import model.Empleado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    // 1. REGISTRAR
    public boolean registrar(Empleado e) {
        String sql = "INSERT INTO AUT_EMPLEADOS (emp_id, emp_cedula, emp_nombre, emp_apellido, emp_direccion, emp_telefono, emp_correo, emp_rol, emp_estado) " +
                     "VALUES (seq_empleados.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, 'A')";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, e.getCedula());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getApellido());
            ps.setString(4, e.getDireccion());
            ps.setString(5, e.getTelefono());
            ps.setString(6, e.getCorreo());
            ps.setString(7, e.getRol()); // 'M' o 'R'
            
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Error registrar empleado: " + ex.getMessage());
            return false;
        }
    }

    // 2. LISTAR TODOS (Activos)
    public List<Empleado> listar() {
        return ejecutarConsulta("SELECT * FROM AUT_EMPLEADOS WHERE emp_estado = 'A' ORDER BY emp_apellido");
    }

    // 3. LISTAR SOLO MECÁNICOS (Para el módulo de Citas)
    public List<Empleado> listarMecanicos() {
        return ejecutarConsulta("SELECT * FROM AUT_EMPLEADOS WHERE emp_estado = 'A' AND emp_rol = 'M' ORDER BY emp_nombre");
    }

    // 4. ACTUALIZAR
    public boolean actualizar(Empleado e) {
        String sql = "UPDATE AUT_EMPLEADOS SET emp_nombre=?, emp_apellido=?, emp_direccion=?, emp_telefono=?, emp_correo=?, emp_rol=? " +
                     "WHERE emp_cedula=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellido());
            ps.setString(3, e.getDireccion());
            ps.setString(4, e.getTelefono());
            ps.setString(5, e.getCorreo());
            ps.setString(6, e.getRol());
            ps.setString(7, e.getCedula());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            return false;
        }
    }

    // 5. ELIMINAR (Lógico)
    public boolean eliminar(String cedula) {
        String sql = "UPDATE AUT_EMPLEADOS SET emp_estado = 'I' WHERE emp_cedula = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cedula);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            return false;
        }
    }

    // 6. BUSCAR POR CÉDULA
    public Empleado buscarPorCedula(String cedula) {
        List<Empleado> lista = ejecutarConsulta("SELECT * FROM AUT_EMPLEADOS WHERE emp_cedula = '" + cedula + "'");
        return lista.isEmpty() ? null : lista.get(0);
    }

    // Auxiliar para no repetir código de lectura
    private List<Empleado> ejecutarConsulta(String sql) {
        List<Empleado> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Empleado e = new Empleado();
                e.setId(rs.getInt("emp_id"));
                e.setCedula(rs.getString("emp_cedula"));
                e.setNombre(rs.getString("emp_nombre"));
                e.setApellido(rs.getString("emp_apellido"));
                e.setDireccion(rs.getString("emp_direccion"));
                e.setTelefono(rs.getString("emp_telefono"));
                e.setCorreo(rs.getString("emp_correo"));
                e.setRol(rs.getString("emp_rol"));
                e.setEstado(rs.getString("emp_estado"));
                lista.add(e);
            }
        } catch (SQLException ex) {
            System.err.println("Error SQL: " + ex.getMessage());
        }
        return lista;
    }
}