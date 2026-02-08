package model;

public class Sesion {
    private final String usuario;
    private final String nombreCompleto;
    private final String rol; // 'A' o 'E'

    public Sesion(String usuario, String nombreCompleto, String rol) {
        this.usuario = usuario;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
    }

    public String getUsuario() { return usuario; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getRol() { return rol; }

    public boolean isAdmin() { return "A".equalsIgnoreCase(rol); }
}

