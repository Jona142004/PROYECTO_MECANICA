 package model;

public class DetalleFactura {
    private int id; // dta_id
    private int idServicio;
    private String nombreServicio; // Para mostrar en la tabla (no se guarda en BD, pero sirve en UI)
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    private double valorIva;
    private double total;
    
    // Auxiliar para saber si este servicio grava IVA (S/N)
    private boolean gravaIva;

    public DetalleFactura() {}

    // Constructor COMPLETO (Usado por la interfaz gráfica al agregar servicio)
    public DetalleFactura(int idServicio, String nombreServicio, int cantidad, double precioUnitario, boolean gravaIva) {
        this.idServicio = idServicio;
        this.nombreServicio = nombreServicio;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.gravaIva = gravaIva;
        
        // Calculamos los montos inmediatamente
        calcular();
    }

    // Método que actualiza los cálculos matemáticos
    public void calcular() {
        this.subtotal = this.cantidad * this.precioUnitario;
        
        if (this.gravaIva) {
            // IVA del 15% (Puedes cambiar a 0.12 si es necesario)
            this.valorIva = this.subtotal * 0.15; 
        } else {
            this.valorIva = 0.0;
        }
        
        this.total = this.subtotal + this.valorIva;
    }

    // --- GETTERS Y SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdServicio() { return idServicio; }
    public void setIdServicio(int idServicio) { this.idServicio = idServicio; }

    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { 
        this.cantidad = cantidad; 
        calcular(); // Si cambia cantidad, recalculamos
    }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { 
        this.precioUnitario = precioUnitario;
        calcular(); // Si cambia precio, recalculamos
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getValorIva() { return valorIva; }
    public void setValorIva(double valorIva) { this.valorIva = valorIva; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    
    public boolean isGravaIva() { return gravaIva; }
    public void setGravaIva(boolean gravaIva) {
        this.gravaIva = gravaIva;
        calcular();
    }
}