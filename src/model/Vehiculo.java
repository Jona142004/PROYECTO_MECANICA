package model;

public class Vehiculo {
    private int id;
    private String placa;
    
    // IDs para la Base de Datos
    private int idMarca;
    private int idModelo;
    private int idCliente;

    // Campos auxiliares para mostrar nombres en la Tabla (JTable)
    private String nombreMarca;
    private String nombreModelo;
    private String nombreCliente;

    public Vehiculo() {}

    public Vehiculo(String placa, int idMarca, int idModelo, int idCliente) {
        this.placa = placa;
        this.idMarca = idMarca;
        this.idModelo = idModelo;
        this.idCliente = idCliente;
    }

    // --- Getters y Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    
    public int getIdMarca() { return idMarca; }
    public void setIdMarca(int idMarca) { this.idMarca = idMarca; }
    
    public int getIdModelo() { return idModelo; }
    public void setIdModelo(int idModelo) { this.idModelo = idModelo; }
    
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getNombreMarca() { return nombreMarca; }
    public void setNombreMarca(String nombreMarca) { this.nombreMarca = nombreMarca; }

    public String getNombreModelo() { return nombreModelo; }
    public void setNombreModelo(String nombreModelo) { this.nombreModelo = nombreModelo; }
    
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
}