package dao;

import db.Conexion;
import model.Servicio;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServicioDAO {

    // 1. REGISTRAR
    public boolean registrar(Servicio s) {
        String sql = "INSERT INTO AUT_SERVICIOS (ser_id, ser_nombre, ser_precio, ser_iva, ser_estado) " +
                     "VALUES (seq_servicios.NEXTVAL, ?, ?, ?, 'A')";
        
        Connection con = Conexion.getConexion();
        if (con == null) {
            System.err.println("Error: No hay conexión a la base de datos.");
            return false;
        }

        try (Connection c = con; // Usamos 'c' para que se cierre sola
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, s.getNombre());
            ps.setDouble(2, s.getPrecio());
            ps.setString(3, s.getIva());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error registrar servicio: " + e.getMessage());
            return false;
        }
    }

    // 2. LISTAR TODOS
    public List<Servicio> listar() {
        List<Servicio> lista = new ArrayList<>();
        String sql = "SELECT * FROM AUT_SERVICIOS ORDER BY ser_estado ASC, ser_nombre ASC";
        
        Connection con = Conexion.getConexion();
        if (con == null) return lista; // Retorna lista vacía si no hay conexión

        try (Connection c = con;
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Servicio s = new Servicio();
                s.setId(rs.getInt("ser_id"));
                s.setNombre(rs.getString("ser_nombre"));
                s.setPrecio(rs.getDouble("ser_precio"));
                s.setIva(rs.getString("ser_iva"));
                s.setEstado(rs.getString("ser_estado"));
                lista.add(s);
            }
        } catch (SQLException e) { 
            System.err.println("Error al listar: " + e.getMessage());
        }
        return lista;
    }

    // 3. ACTUALIZAR
    public boolean actualizar(Servicio s) {
        String sql = "UPDATE AUT_SERVICIOS SET ser_nombre=?, ser_precio=?, ser_iva=? WHERE ser_id=?";
        
        Connection con = Conexion.getConexion();
        if (con == null) return false;

        try (Connection c = con;
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getNombre());
            ps.setDouble(2, s.getPrecio());
            ps.setString(3, s.getIva());
            ps.setInt(4, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // 4. ELIMINAR (Lógico: Inactivar)
    public boolean eliminar(int id) {
        String sql = "UPDATE AUT_SERVICIOS SET ser_estado = 'I' WHERE ser_id = ?";
        
        Connection con = Conexion.getConexion();
        if (con == null) return false;

        try (Connection c = con;
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
    
    // 5. BUSCAR POR NOMBRE
    public Servicio buscarPorNombre(String nombre) {
        String sql = "SELECT * FROM AUT_SERVICIOS WHERE UPPER(ser_nombre) LIKE ?";
        
        Connection con = Conexion.getConexion();
        if (con == null) return null;

        try (Connection c = con;
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + nombre.toUpperCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    Servicio s = new Servicio();
                    s.setId(rs.getInt("ser_id"));
                    s.setNombre(rs.getString("ser_nombre"));
                    s.setPrecio(rs.getDouble("ser_precio"));
                    s.setIva(rs.getString("ser_iva"));
                    s.setEstado(rs.getString("ser_estado"));
                    return s;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}