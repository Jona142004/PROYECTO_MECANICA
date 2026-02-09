package model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Factura {
    private int id;
    private String numero;
    private Date fecha;
    private double subtotal;
    private double iva;
    private double total;
    
    // IDs foráneos (Relaciones con otras tablas)
    private int idCliente;
    private int idUsuario;
    
    // Lista de detalles (Relación Maestro-Detalle)
    // Inicializamos la lista para evitar NullPointerException si se pide vacía
    private List<DetalleFactura> detalles = new ArrayList<>();

    public Factura() {}

    // --- GETTERS Y SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getIva() { return iva; }
    public void setIva(double iva) { this.iva = iva; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public List<DetalleFactura> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleFactura> detalles) { this.detalles = detalles; }
    
    // Método auxiliar para agregar un detalle a la vez
    public void agregarDetalle(DetalleFactura detalle) {
        this.detalles.add(detalle);
    }
}