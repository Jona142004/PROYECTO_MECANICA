package model;

public class Sesion {
    // --- VARIABLE GLOBAL ---
    private static Sesion sesionActual;

    public static void setSesionActual(Sesion s) {
        sesionActual = s;
    }

    public static Sesion get() {
        return sesionActual;
    }
    // -----------------------

    private int id; // <--- ESTE ES EL DATO NUEVO IMPORTANTE
    private final String usuario;
    private final String nombreCompleto;
    private final String rol;

    // CONSTRUCTOR DE 4 PARÁMETROS (ID, Usuario, Nombre, Rol)
    public Sesion(int id, String usuario, String nombreCompleto, String rol) {
        this.id = id;
        this.usuario = usuario;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
    }

    public int getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getRol() { return rol; }
    public boolean isAdmin() { return "A".equalsIgnoreCase(rol); }
}