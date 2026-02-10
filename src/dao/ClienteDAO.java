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
            ps.setString(7, "A"); 
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error registrar: " + e.getMessage());
            return false;
        }
    }

    // 2. ACTUALIZAR (Incluye estado)
    public boolean actualizar(Cliente c) {
        String sql = "UPDATE AUT_CLIENTES SET cli_nombre=?, cli_apellido=?, cli_direccion=?, cli_telefono=?, cli_correo=?, cli_estado=? " +
                     "WHERE cli_cedula=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getDireccion());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getCorreo());
            ps.setString(6, c.getEstado()); 
            ps.setString(7, c.getCedula()); 
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizar: " + e.getMessage());
            return false;
        }
    }

    /**
     * 3. ELIMINAR INTELIGENTE
     * Retorna:
     * 1 = Eliminado TOTALMENTE (Sin historial).
     * 2 = Pasado a INACTIVO (Con historial).
     * 0 = Error.
     */
    public int eliminar(String cedula) {
        String sqlDelete = "DELETE FROM AUT_CLIENTES WHERE cli_cedula = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sqlDelete)) {
            ps.setString(1, cedula);
            if (ps.executeUpdate() > 0) return 1; 
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) return anularLogico(cedula);
            System.err.println("Error eliminar físico: " + e.getMessage());
        }
        return 0; 
    }

    private int anularLogico(String cedula) {
        String sql = "UPDATE AUT_CLIENTES SET cli_estado = 'I' WHERE cli_cedula = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cedula);
            return ps.executeUpdate() > 0 ? 2 : 0; 
        } catch (SQLException e) { return 0; }
    }

    // 4. BUSCAR POR CÉDULA
    public Cliente buscarPorCedula(String cedula) {
        String sql = "SELECT * FROM AUT_CLIENTES WHERE cli_cedula = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // 5. LISTAR TODOS (Para ClientesFrame - ADMIN)
    // Muestra Activos e Inactivos
    public List<Cliente> listarTodos() {
        return ejecutarConsulta("SELECT * FROM AUT_CLIENTES ORDER BY cli_apellido");
    }

    // 6. LISTAR SOLO ACTIVOS (Para ClienteDialog - SELECCIÓN)
    // Esto es lo que te faltaba y daba error en el Dialog
    public List<Cliente> listarActivos() {
        return ejecutarConsulta("SELECT * FROM AUT_CLIENTES WHERE cli_estado = 'A' ORDER BY cli_apellido");
    }

    // 7. BUSCAR POR FILTRO (Para ClienteDialog - BUSCADOR)
    // Esto también te faltaba
    public List<Cliente> buscarPorFiltro(String texto) {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM AUT_CLIENTES WHERE cli_estado = 'A' " +
                     "AND (cli_cedula LIKE ? OR UPPER(cli_nombre) LIKE ? OR UPPER(cli_apellido) LIKE ?) " +
                     "ORDER BY cli_apellido";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            String pattern = "%" + texto.toUpperCase() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error buscar filtro: " + e.getMessage());
        }
        return lista;
    }

    // --- MÉTODOS PRIVADOS ---
    private List<Cliente> ejecutarConsulta(String sql) {
        List<Cliente> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("cli_id"));
        c.setCedula(rs.getString("cli_cedula"));
        c.setNombre(rs.getString("cli_nombre"));
        c.setApellido(rs.getString("cli_apellido"));
        c.setDireccion(rs.getString("cli_direccion"));
        c.setTelefono(rs.getString("cli_telefono"));
        c.setCorreo(rs.getString("cli_correo"));
        c.setEstado(rs.getString("cli_estado"));
        return c;
    }
}