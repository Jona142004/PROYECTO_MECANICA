package dao;

import db.Conexion;
import model.Cita;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CitaDAO {

    // 1. AGENDAR CITA
    public boolean agendar(Cita c) {
        String sql = "INSERT INTO AUT_CITAS (cit_id, cit_fecha, cit_hora, cit_cancelada, AUT_VEHICULOS_veh_id, AUT_EMPLEADOS_emp_id) " +
                     "VALUES (seq_citas.NEXTVAL, ?, ?, 'N', ?, ?)";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setDate(1, c.getFecha());
            ps.setTimestamp(2, c.getHora());
            ps.setInt(3, c.getIdVehiculo());
            ps.setInt(4, c.getIdMecanico());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1 || e.getMessage().contains("UK_CITAS_MECANICO")) {
                System.err.println("Error: El mecánico ya tiene una cita a esa hora.");
                return false; 
            }
            System.err.println("Error al agendar: " + e.getMessage());
            return false;
        }
    }

    // 2. LISTAR TODAS (Con JOINs para mostrar nombres)
    public List<Cita> listar() {
        List<Cita> lista = new ArrayList<>();
        String sql = "SELECT ci.cit_id, ci.cit_fecha, ci.cit_hora, ci.cit_cancelada, " +
                     "v.veh_placa, ma.mar_nombre, mo.mod_nombre, " +
                     "cl.cli_nombre || ' ' || cl.cli_apellido AS cliente, " +
                     "e.emp_nombre || ' ' || e.emp_apellido AS mecanico, " +
                     "ci.AUT_VEHICULOS_veh_id, ci.AUT_EMPLEADOS_emp_id " +
                     "FROM AUT_CITAS ci " +
                     "JOIN AUT_VEHICULOS v ON ci.AUT_VEHICULOS_veh_id = v.veh_id " +
                     "JOIN AUT_MARCAS ma ON v.AUT_MARCAS_mar_id = ma.mar_id " +
                     "JOIN AUT_MODELOS mo ON v.AUT_MODELOS_mod_id = mo.mod_id " +
                     "JOIN AUT_CLIENTES cl ON v.AUT_CLIENTES_cli_id = cl.cli_id " +
                     "JOIN AUT_EMPLEADOS e ON ci.AUT_EMPLEADOS_emp_id = e.emp_id " +
                     "ORDER BY ci.cit_hora DESC";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Cita c = new Cita();
                c.setId(rs.getInt("cit_id"));
                c.setFecha(rs.getDate("cit_fecha"));
                c.setHora(rs.getTimestamp("cit_hora"));
                
                // Mapeo de estado: N = Activa, S = Cancelada
                String estadoDB = rs.getString("cit_cancelada");
                c.setEstado(estadoDB.equals("N") ? "ACTIVA" : "CANCELADA");
                
                c.setPlaca(rs.getString("veh_placa"));
                c.setMarcaModelo(rs.getString("mar_nombre") + " " + rs.getString("mod_nombre"));
                c.setCliente(rs.getString("cliente"));
                c.setNombreMecanico(rs.getString("mecanico"));
                
                c.setIdVehiculo(rs.getInt("AUT_VEHICULOS_veh_id"));
                c.setIdMecanico(rs.getInt("AUT_EMPLEADOS_emp_id"));
                
                lista.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 3. CANCELAR (Lógico: UPDATE cit_cancelada = 'S')
    public boolean cancelar(int id) {
        String sql = "UPDATE AUT_CITAS SET cit_cancelada = 'S' WHERE cit_id = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 4. BUSCAR POR ID
    public Cita buscarPorId(int id) {
        // Implementación similar a listar() pero con WHERE id = ?
        // Por brevedad, usaremos los datos de la tabla en la UI, 
        // pero para editar correctamente deberías implementarlo.
        return null; 
    }
}