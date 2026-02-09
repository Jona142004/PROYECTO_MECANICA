package dao;

import db.Conexion;
import model.DetalleFactura;
import model.Factura;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacturaDAO {

    // ==========================================
    // 1. GUARDAR FACTURA (Tu código original)
    // ==========================================
    public boolean guardarFactura(Factura f) {
        Connection con = Conexion.getConexion();
        if (con == null) return false;

        PreparedStatement psFactura = null;
        PreparedStatement psDetalle = null;
        ResultSet rsKeys = null;

        try {
            con.setAutoCommit(false); // INICIAR TRANSACCIÓN

            // INSERTAR CABECERA
            String sqlFac = "INSERT INTO AUT_FACTURAS " +
                            "(fac_id, fac_numero, fac_fecha_emision, fac_subtotal, fac_iva, fac_total, AUT_CLIENTES_cli_id, AUT_USUARIOS_usu_id, fac_estado) " +
                            "VALUES (seq_facturas.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, 'A')";
            
            // Pedimos recuperar el ID generado
            psFactura = con.prepareStatement(sqlFac, new String[]{"fac_id"}); 
            
            psFactura.setString(1, f.getNumero());
            psFactura.setDate(2, f.getFecha());
            psFactura.setDouble(3, f.getSubtotal());
            psFactura.setDouble(4, f.getIva());
            psFactura.setDouble(5, f.getTotal());
            psFactura.setInt(6, f.getIdCliente());
            psFactura.setInt(7, f.getIdUsuario());
            
            int rows = psFactura.executeUpdate();
            if (rows == 0) throw new SQLException("No se guardó la cabecera.");

            // RECUPERAR ID
            rsKeys = psFactura.getGeneratedKeys();
            int idFactura = 0;
            if (rsKeys.next()) {
                idFactura = rsKeys.getInt(1);
            } else {
                throw new SQLException("No se obtuvo ID factura.");
            }

            // INSERTAR DETALLES
            String sqlDet = "INSERT INTO AUT_DETALLES_FACTURAS " +
                            "(dta_id, dta_cantidad, dta_precio_unitario, dta_subtotal, dta_valor_iva, dta_total_linea, AUT_FACTURAS_fac_id, AUT_SERVICIOS_ser_id) " +
                            "VALUES (seq_det_facturas.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
            
            psDetalle = con.prepareStatement(sqlDet);

            for (DetalleFactura d : f.getDetalles()) {
                psDetalle.setInt(1, d.getCantidad());
                psDetalle.setDouble(2, d.getPrecioUnitario());
                psDetalle.setDouble(3, d.getSubtotal());
                psDetalle.setDouble(4, d.getValorIva());
                psDetalle.setDouble(5, d.getTotal());
                psDetalle.setInt(6, idFactura);
                psDetalle.setInt(7, d.getIdServicio());
                
                psDetalle.executeUpdate();
            }

            con.commit(); // CONFIRMAR
            return true;

        } catch (SQLException e) {
            System.err.println("Error Guardar: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try {
                if (rsKeys != null) rsKeys.close();
                if (psFactura != null) psFactura.close();
                if (psDetalle != null) psDetalle.close();
                if (con != null) { con.setAutoCommit(true); con.close(); }
            } catch (SQLException e) {}
        }
    }

    // ==========================================
    // 2. GENERAR NÚMERO (Tu código original)
    // ==========================================
    public String generarNumeroFactura() {
        String numero = "001-001-000000001";
        String sql = "SELECT LPAD(count(*) + 1, 9, '0') FROM AUT_FACTURAS";
        
        try (Connection con = Conexion.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) numero = "001-001-" + rs.getString(1);
        } catch(Exception e) { e.printStackTrace(); }
        return numero;
    }

    // ==========================================
    // 3. NUEVO: LISTAR FACTURAS (Para Buscar)
    // ==========================================
    public List<Factura> listar() {
        List<Factura> lista = new ArrayList<>();
        // Traemos también el nombre del cliente con un JOIN
        String sql = "SELECT f.fac_id, f.fac_numero, f.fac_fecha_emision, f.fac_total, f.fac_estado, " +
                     "c.cli_nombre, c.cli_apellido " +
                     "FROM AUT_FACTURAS f " +
                     "JOIN AUT_CLIENTES c ON f.AUT_CLIENTES_cli_id = c.cli_id " +
                     "ORDER BY f.fac_id DESC"; // Las más recientes primero

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while(rs.next()) {
                Factura f = new Factura();
                f.setId(rs.getInt("fac_id"));
                f.setNumero(rs.getString("fac_numero"));
                f.setFecha(rs.getDate("fac_fecha_emision"));
                f.setTotal(rs.getDouble("fac_total"));
                // Guardamos el estado ('A'ctivo o 'I'nactivo)
                // Nota: Asegúrate de tener el método setEstado en tu modelo Factura si quieres usarlo visualmente
                // f.setEstado(rs.getString("fac_estado")); 
                
                // Usamos un truco: guardamos el nombre del cliente en una variable temporal si tu modelo no tiene campo cliente
                // O mejor, asumimos que solo mostramos ID, Numero y Total en la tabla básica.
                lista.add(f);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    // ==========================================
    // 4. NUEVO: LISTAR DETALLES (Para ver la factura)
    // ==========================================
    public List<DetalleFactura> listarDetalles(int idFactura) {
        List<DetalleFactura> lista = new ArrayList<>();
        String sql = "SELECT d.*, s.ser_nombre, s.ser_iva " +
                     "FROM AUT_DETALLES_FACTURAS d " +
                     "JOIN AUT_SERVICIOS s ON d.AUT_SERVICIOS_ser_id = s.ser_id " +
                     "WHERE d.AUT_FACTURAS_fac_id = ?";
                     
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idFactura);
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    // Convertimos 'S'/'N' a booleano
                    boolean grava = "S".equalsIgnoreCase(rs.getString("ser_iva"));
                    
                    DetalleFactura d = new DetalleFactura(
                        rs.getInt("AUT_SERVICIOS_ser_id"),
                        rs.getString("ser_nombre"),
                        rs.getInt("dta_cantidad"),
                        rs.getDouble("dta_precio_unitario"),
                        grava
                    );
                    lista.add(d);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    // ==========================================
    // 5. NUEVO: ANULAR FACTURA
    // ==========================================
    public boolean anular(int idFactura) {
        // Cambiamos estado a 'I' (Inactivo/Anulado)
        String sql = "UPDATE AUT_FACTURAS SET fac_estado = 'I' WHERE fac_id = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idFactura);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }
}