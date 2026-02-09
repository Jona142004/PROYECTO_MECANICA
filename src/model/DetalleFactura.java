package model;

public class DetalleFactura {
    private int id; // ID del detalle (dta_id), opcional si solo insertamos
    private int idServicio;
    private String nombreServicio; // Para mostrar en la tabla visualmente
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    private double valorIva;
    private double total;
    
    // Auxiliar para saber si calculamos IVA o no
    private boolean gravaIva;

    public DetalleFactura() {}

    // Constructor completo para facilitar la creación desde la interfaz
    public DetalleFactura(int idServicio, String nombreServicio, int cantidad, double precioUnitario, boolean gravaIva) {
        this.idServicio = idServicio;
        this.nombreServicio = nombreServicio;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.gravaIva = gravaIva;
        calcular(); // Calcular montos automáticamente al crear
    }

    // Método para recalcular valores matemáticos
    public void calcular() {
        this.subtotal = this.cantidad * this.precioUnitario;
        
        // IVA del 15% (según tu lógica actual, ajustable si cambia la ley)
        if (this.gravaIva) {
            this.valorIva = this.subtotal * 0.15; 
            // Nota: Podrías usar 0.12 o 0.15 según tu país. 
            // Si necesitas precisión exacta de moneda, BigDecimal es mejor, 
            // pero double funciona bien para este nivel escolar/académico.
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
        calcular(); // Recalcular si cambia la cantidad
    }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { 
        this.precioUnitario = precioUnitario;
        calcular(); // Recalcular si cambia el precio
    }

    public double getSubtotal() { return subtotal; }
    // No ponemos setSubtotal público porque es calculado

    public double getValorIva() { return valorIva; }
    
    public double getTotal() { return total; }
    
    public boolean isGravaIva() { return gravaIva; }
    public void setGravaIva(boolean gravaIva) {
        this.gravaIva = gravaIva;
        calcular();
    }
}