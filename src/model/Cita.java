package model;

import java.sql.Date;
import java.sql.Timestamp;

public class Cita {
    private int id;
    private Date fecha;         // Solo la fecha (para búsquedas)
    private Timestamp hora;     // Fecha + Hora exacta (para la agenda)
    private String estado;      // 'S' (Cancelada) o 'N' (Activa)
    
    // IDs (Foreign Keys)
    private int idVehiculo;
    private int idMecanico;

    // Datos para mostrar en la Tabla (JOINs)
    private String placa;
    private String marcaModelo; // Ej: "Toyota Yaris"
    private String cliente;
    private String nombreMecanico;

    public Cita() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    
    public Timestamp getHora() { return hora; }
    public void setHora(Timestamp hora) { this.hora = hora; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }
    
    public int getIdMecanico() { return idMecanico; }
    public void setIdMecanico(int idMecanico) { this.idMecanico = idMecanico; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    
    public String getMarcaModelo() { return marcaModelo; }
    public void setMarcaModelo(String marcaModelo) { this.marcaModelo = marcaModelo; }
    
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    
    public String getNombreMecanico() { return nombreMecanico; }
    public void setNombreMecanico(String nombreMecanico) { this.nombreMecanico = nombreMecanico; }
}