package ui.servicios;

public class ServicioItem {
    private final String codigo;
    private final String nombre;
    private final double precio;
    private final boolean gravaIva;

    public ServicioItem(String codigo, String nombre, double precio, boolean gravaIva) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.gravaIva = gravaIva;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public boolean isGravaIva() { return gravaIva; }
}
