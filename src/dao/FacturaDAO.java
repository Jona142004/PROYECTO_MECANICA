package dao;

import db.Conexion;
import model.DetalleFactura;
import model.Factura;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacturaDAO {

    // ==========================================
    // 1. GUARDAR FACTURA (Transaccional)
    // ==========================================
    public boolean guardarFactura(Factura f) {
        Connection con = Conexion.getConexion();
        if (con == null) return false;

        PreparedStatement psFactura = null;
        PreparedStatement psDetalle = null;
        ResultSet rsKeys = null;

        try {
            con.setAutoCommit(false); // INICIAR TRANSACCIÓN

            // NOTA: Asumimos que ejecutaste el script para agregar 'fac_estado' 
            // Si no lo hiciste, borra ", fac_estado" y ", 'A'" del SQL.
            String sqlFac = "INSERT INTO AUT_FACTURAS " +
                            "(fac_id, fac_numero, fac_fecha_emision, fac_subtotal, fac_iva, fac_total, AUT_CLIENTES_cli_id, AUT_USUARIOS_usu_id, fac_estado) " +
                            "VALUES (seq_facturas.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, 'A')";
            
            // Oracle: Pedimos FAC_ID en mayúsculas
            psFactura = con.prepareStatement(sqlFac, new String[]{"FAC_ID"}); 
            
            psFactura.setString(1, f.getNumero());
            psFactura.setDate(2, f.getFecha());
            psFactura.setDouble(3, f.getSubtotal());
            psFactura.setDouble(4, f.getIva());
            psFactura.setDouble(5, f.getTotal());
            psFactura.setInt(6, f.getIdCliente());
            psFactura.setInt(7, f.getIdUsuario());
            
            int rows = psFactura.executeUpdate();
            if (rows == 0) throw new SQLException("No se guardó la cabecera.");

            // RECUPERAR ID GENERADO
            rsKeys = psFactura.getGeneratedKeys();
            int idFactura = 0;
            if (rsKeys.next()) {
                idFactura = rsKeys.getInt(1);
            } else {
                throw new SQLException("No se obtuvo el ID de la factura.");
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

            con.commit(); // CONFIRMAR TRANSACCIÓN
            return true;

        } catch (SQLException e) {
            e.printStackTrace(); 
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
    // 2. GENERAR NÚMERO (Ajustado a 12 caracteres)
    // ==========================================
    public String generarNumeroFactura() {
        // Formato: 001-001-XXXX (Total 12 caracteres)
        String numero = "001-001-0001"; 
        
        // Usamos LPAD con 4 ceros para que quepa (8 prefijo + 4 nums = 12)
        String sql = "SELECT LPAD(count(*) + 1, 4, '0') FROM AUT_FACTURAS";
        
        try (Connection con = Conexion.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            if (rs.next()) {
                numero = "001-001-" + rs.getString(1); 
            }
        } catch(Exception e) { e.printStackTrace(); }
        return numero;
    }

    // ==========================================
    // 3. OBTENER TOTAL VENTAS (Acumulador)
    // ==========================================
    public double obtenerTotalVentas() {
        double total = 0;
        // Solo sumamos las activas ('A')
        String sql = "SELECT SUM(fac_total) FROM AUT_FACTURAS WHERE fac_estado = 'A'";
        try (Connection con = Conexion.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return total;
    }

    // ==========================================
    // 4. LISTAR ACTIVAS (Para anular)
    // ==========================================
    public List<Factura> listarActivas() {
        return ejecutarConsulta("SELECT f.fac_id, f.fac_numero, f.fac_fecha_emision, f.fac_total, " +
                                "c.cli_nombre, c.cli_apellido, f.fac_estado " +
                                "FROM AUT_FACTURAS f " +
                                "JOIN AUT_CLIENTES c ON f.AUT_CLIENTES_cli_id = c.cli_id " +
                                "WHERE f.fac_estado = 'A' " +
                                "ORDER BY f.fac_id DESC");
    }

    // ==========================================
    // 5. LISTAR POR CLIENTE (Filtro anular)
    // ==========================================
    public List<Factura> listarPorCliente(int idCliente) {
        return ejecutarConsulta("SELECT f.fac_id, f.fac_numero, f.fac_fecha_emision, f.fac_total, " +
                                "c.cli_nombre, c.cli_apellido, f.fac_estado " +
                                "FROM AUT_FACTURAS f " +
                                "JOIN AUT_CLIENTES c ON f.AUT_CLIENTES_cli_id = c.cli_id " +
                                "WHERE f.fac_estado = 'A' AND f.AUT_CLIENTES_cli_id = " + idCliente + " " +
                                "ORDER BY f.fac_id DESC");
    }

    // ==========================================
    // 6. ANULAR (Update estado)
    // ==========================================
    public boolean anular(int idFactura) {
        String sql = "UPDATE AUT_FACTURAS SET fac_estado = 'I' WHERE fac_id = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idFactura);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    // ==========================================
    // 7. LISTAR DETALLES (Para cargar factura)
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

    // --- AUXILIAR PRIVADO ---
    private List<Factura> ejecutarConsulta(String sql) {
        List<Factura> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
                Factura f = new Factura();
                f.setId(rs.getInt("fac_id"));
                f.setNumero(rs.getString("fac_numero"));
                f.setFecha(rs.getDate("fac_fecha_emision"));
                f.setTotal(rs.getDouble("fac_total"));
                // Guardamos nombre cliente en un campo auxiliar o reutilizamos uno existente para mostrar en tabla
                // Suponemos que Factura tiene un setAuxNombreCliente o lo manejamos visualmente
                f.setAuxNombreCliente(rs.getString("cli_nombre") + " " + rs.getString("cli_apellido"));
                lista.add(f);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}