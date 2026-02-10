package dao;

import db.Conexion;
import model.Cita;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CitaDAO {

    // 1. AGENDAR CITA
    public boolean agendar(Cita c) {
        // Usamos seq_citas.NEXTVAL. cit_cancelada por defecto es 'N'
        String sql = "INSERT INTO AUT_CITAS (cit_id, cit_fecha, cit_hora, cit_cancelada, AUT_VEHICULOS_veh_id, AUT_EMPLEADOS_emp_id) " +
                     "VALUES (seq_citas.NEXTVAL, ?, ?, 'N', ?, ?)";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            // Convertimos java.util.Date a java.sql.Date para la fecha
            ps.setDate(1, new java.sql.Date(c.getFecha().getTime()));
            
            // El Timestamp lleva la fecha y la hora combinadas
            ps.setTimestamp(2, c.getHora());
            
            ps.setInt(3, c.getIdVehiculo());
            ps.setInt(4, c.getIdMecanico());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            // Manejo del error del Trigger o Index Unique (Mecánico ocupado)
            if (e.getErrorCode() == 20002 || e.getMessage().contains("UK_CITAS_MECANICO")) {
                System.err.println("Error: El mecánico no está disponible o no es válido.");
            } else {
                System.err.println("Error al agendar: " + e.getMessage());
            }
            return false;
        }
    }

    // 2. LISTAR TODAS (Con JOINs para mostrar nombres en la tabla)
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
                
                String est = rs.getString("cit_cancelada");
                c.setEstado(est.equals("N") ? "ACTIVA" : "CANCELADA");
                
                // Datos visuales para la tabla (usando setters auxiliares en modelo Cita si existen, 
                // o guardándolos para mostrarlos en la UI)
                c.setPlaca(rs.getString("veh_placa"));
                c.setMarcaModelo(rs.getString("mar_nombre") + " " + rs.getString("mod_nombre"));
                c.setCliente(rs.getString("cliente"));
                c.setNombreMecanico(rs.getString("mecanico"));
                
                // IDs para edición
                c.setIdVehiculo(rs.getInt("AUT_VEHICULOS_veh_id"));
                c.setIdMecanico(rs.getInt("AUT_EMPLEADOS_emp_id"));
                
                lista.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 3. CANCELAR (Lógico)
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
}