package model;

public class Usuario {
    private int id;
    private String usuario;
    private String clave;
    private String rol;    // 'A' o 'E'
    private String estado; // 'A' o 'I'
    
    // Datos del empleado dueño de la cuenta
    private int idEmpleado;
    private String nombreEmpleado;

    public Usuario() {}

    // --- GETTERS Y SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }

    // --- ESTE ES EL MÉTODO QUE TE FALTABA ---
    public String getRolNombre() {
        if ("A".equalsIgnoreCase(rol)) {
            return "ADMINISTRADOR";
        } else if ("E".equalsIgnoreCase(rol)) {
            return "EMPLEADO";
        }
        return rol; // Retorna la letra si no coincide
    }
}