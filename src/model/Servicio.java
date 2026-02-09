package model;

public class Servicio {
    private int id;
    private String nombre;
    private double precio;
    private String iva;    // 'S' (Sí) o 'N' (No)
    private String estado; // 'A' (Activo) o 'I' (Inactivo)

    public Servicio() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getIva() { return iva; }
    public void setIva(String iva) { this.iva = iva; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}