package dao;

import db.Conexion;
import model.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    // 1. REGISTRAR CLIENTE
    public boolean registrar(Cliente c) {
        // Usamos la secuencia seq_clientes.NEXTVAL para el ID
        String sql = "INSERT INTO AUT_CLIENTES (cli_id, cli_cedula, cli_nombre, cli_apellido, cli_direccion, cli_telefono, cli_correo, cli_estado) " +
                     "VALUES (seq_clientes.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, c.getCedula());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getApellido());
            ps.setString(4, c.getDireccion());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getCorreo());
            ps.setString(7, "A"); // Siempre activo al crear
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar cliente: " + e.getMessage());
            return false;
        }
    }

    // 2. BUSCAR POR CÉDULA
    public Cliente buscarPorCedula(String cedula) {
        String sql = "SELECT * FROM AUT_CLIENTES WHERE cli_cedula = ? AND cli_estado = 'A'";
        Cliente c = null;
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, cedula);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    c = new Cliente();
                    c.setId(rs.getInt("cli_id"));
                    c.setCedula(rs.getString("cli_cedula"));
                    c.setNombre(rs.getString("cli_nombre"));
                    c.setApellido(rs.getString("cli_apellido"));
                    c.setDireccion(rs.getString("cli_direccion"));
                    c.setTelefono(rs.getString("cli_telefono"));
                    c.setCorreo(rs.getString("cli_correo"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar: " + e.getMessage());
        }
        return c;
    }

    // 3. ACTUALIZAR
    public boolean actualizar(Cliente c) {
        String sql = "UPDATE AUT_CLIENTES SET cli_nombre=?, cli_apellido=?, cli_direccion=?, cli_telefono=?, cli_correo=? " +
                     "WHERE cli_cedula=?";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getDireccion());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getCorreo());
            ps.setString(6, c.getCedula()); // El WHERE va al final
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar: " + e.getMessage());
            return false;
        }
    }

    // 4. ELIMINAR (Lógico: Cambiar estado a 'I')
    public boolean eliminar(String cedula) {
        String sql = "UPDATE AUT_CLIENTES SET cli_estado = 'I' WHERE cli_cedula = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cedula);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar: " + e.getMessage());
            return false;
        }
    }
    
    // 5. LISTAR TODOS LOS ACTIVOS (Para llenar la tabla)
    public List<Cliente> listarActivos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM AUT_CLIENTES WHERE cli_estado = 'A' ORDER BY cli_apellido";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getInt("cli_id"));
                c.setCedula(rs.getString("cli_cedula"));
                c.setNombre(rs.getString("cli_nombre"));
                c.setApellido(rs.getString("cli_apellido"));
                c.setDireccion(rs.getString("cli_direccion"));
                c.setTelefono(rs.getString("cli_telefono"));
                c.setCorreo(rs.getString("cli_correo"));
                c.setEstado(rs.getString("cli_estado"));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar: " + e.getMessage());
        }
        return lista;
    }
}
