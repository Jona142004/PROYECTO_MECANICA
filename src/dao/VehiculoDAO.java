package dao;

import db.Conexion;
import model.Vehiculo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {

    // --- MÉTODOS CRUD DE VEHÍCULO ---

    public boolean registrar(Vehiculo v) {
        String sql = "INSERT INTO AUT_VEHICULOS (veh_id, veh_placa, AUT_MARCAS_mar_id, AUT_MODELOS_mod_id, AUT_CLIENTES_cli_id) " +
                     "VALUES (seq_vehiculos.NEXTVAL, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getPlaca());
            ps.setInt(2, v.getIdMarca());
            ps.setInt(3, v.getIdModelo());
            ps.setInt(4, v.getIdCliente());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error registrar: " + e.getMessage());
            return false;
        }
    }

    public List<Vehiculo> listar() {
        List<Vehiculo> lista = new ArrayList<>();
        // JOIN para traer los nombres reales en lugar de números
        String sql = "SELECT v.veh_id, v.veh_placa, m.mar_nombre, mo.mod_nombre, c.cli_nombre, c.cli_apellido " +
                     "FROM AUT_VEHICULOS v " +
                     "JOIN AUT_MARCAS m ON v.AUT_MARCAS_mar_id = m.mar_id " +
                     "JOIN AUT_MODELOS mo ON v.AUT_MODELOS_mod_id = mo.mod_id " +
                     "JOIN AUT_CLIENTES c ON v.AUT_CLIENTES_cli_id = c.cli_id";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Vehiculo v = new Vehiculo();
                v.setId(rs.getInt("veh_id"));
                v.setPlaca(rs.getString("veh_placa"));
                v.setNombreMarca(rs.getString("mar_nombre"));
                v.setNombreModelo(rs.getString("mod_nombre"));
                v.setNombreCliente(rs.getString("cli_nombre") + " " + rs.getString("cli_apellido"));
                lista.add(v);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
    
    public Vehiculo buscarPorPlaca(String placa) {
        String sql = "SELECT * FROM AUT_VEHICULOS WHERE veh_placa = ?";
        Vehiculo v = null;
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    v = new Vehiculo();
                    v.setId(rs.getInt("veh_id"));
                    v.setPlaca(rs.getString("veh_placa"));
                    v.setIdMarca(rs.getInt("AUT_MARCAS_mar_id"));
                    v.setIdModelo(rs.getInt("AUT_MODELOS_mod_id"));
                    v.setIdCliente(rs.getInt("AUT_CLIENTES_cli_id"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return v;
    }

    // --- MÉTODOS AUXILIARES PARA LOS COMBOBOX (MARCAS Y MODELOS) ---

    public List<OpcionCombo> obtenerMarcas() {
        return listarOpciones("SELECT mar_id, mar_nombre FROM AUT_MARCAS ORDER BY mar_nombre", "mar_id", "mar_nombre");
    }

    public List<OpcionCombo> obtenerModelos() {
        return listarOpciones("SELECT mod_id, mod_nombre FROM AUT_MODELOS ORDER BY mod_nombre", "mod_id", "mod_nombre");
    }

    // Método genérico privado para no repetir código
    private List<OpcionCombo> listarOpciones(String sql, String colId, String colNombre) {
        List<OpcionCombo> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new OpcionCombo(rs.getInt(colId), rs.getString(colNombre)));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    // --- CLASE INTERNA PARA LLENAR EL COMBOBOX ---
    // Esto evita tener que crear archivos Marca.java y Modelo.java aparte
    public static class OpcionCombo {
        private int id;
        private String nombre;

        public OpcionCombo(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() { return id; }

        // El ComboBox usa este método para saber qué mostrar en pantalla
        @Override
        public String toString() {
            return nombre;
        }
        
        // Necesario para que el combobox sepa comparar objetos al seleccionar
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OpcionCombo) {
                return this.id == ((OpcionCombo) obj).id;
            }
            return false;
        }
    }
    // 4. ACTUALIZAR VEHÍCULO
    public boolean actualizar(Vehiculo v) {
        String sql = "UPDATE AUT_VEHICULOS SET veh_placa=?, AUT_MARCAS_mar_id=?, AUT_MODELOS_mod_id=?, AUT_CLIENTES_cli_id=? " +
                     "WHERE veh_id=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getPlaca());
            ps.setInt(2, v.getIdMarca());
            ps.setInt(3, v.getIdModelo());
            ps.setInt(4, v.getIdCliente());
            ps.setInt(5, v.getId()); // Usamos el ID para saber cuál actualizar
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizar: " + e.getMessage());
            return false;
        }
    }

    // 5. ELIMINAR VEHÍCULO
    public boolean eliminar(int id) {
        String sql = "DELETE FROM AUT_VEHICULOS WHERE veh_id = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error eliminar: " + e.getMessage());
            return false;
        }
    }
    // 4. BUSCAR PARA EL DIÁLOGO (Por Placa o Cliente)
    public List<Vehiculo> buscarPorFiltro(String texto) {
        List<Vehiculo> lista = new ArrayList<>();
        String sql = "SELECT v.veh_id, v.veh_placa, m.mar_nombre, mo.mod_nombre, c.cli_nombre, c.cli_apellido " +
                     "FROM AUT_VEHICULOS v " +
                     "JOIN AUT_MARCAS m ON v.AUT_MARCAS_mar_id = m.mar_id " +
                     "JOIN AUT_MODELOS mo ON v.AUT_MODELOS_mod_id = mo.mod_id " +
                     "JOIN AUT_CLIENTES c ON v.AUT_CLIENTES_cli_id = c.cli_id " +
                     "WHERE UPPER(v.veh_placa) LIKE ? OR UPPER(c.cli_apellido) LIKE ?";
        
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            String pattern = "%" + texto.toUpperCase() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vehiculo v = new Vehiculo();
                    v.setId(rs.getInt("veh_id"));
                    v.setPlaca(rs.getString("veh_placa"));
                    v.setNombreMarca(rs.getString("mar_nombre"));
                    v.setNombreModelo(rs.getString("mod_nombre"));
                    v.setNombreCliente(rs.getString("cli_nombre") + " " + rs.getString("cli_apellido"));
                    lista.add(v);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}