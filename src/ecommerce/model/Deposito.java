package ecommerce.model;

public class Deposito {
    private String idDeposito;
    private String nombre;
    private String idNodoLogistico;
    private int capacidadMaxima;
    private int capacidadDisponible;

    public Deposito(String idDeposito, String nombre, String idNodoLogistico, int capacidadMaxima, int capacidadDisponible) {
        this.idDeposito = idDeposito;
        this.nombre = nombre;
        this.idNodoLogistico = idNodoLogistico;
        this.capacidadMaxima = capacidadMaxima;
        this.capacidadDisponible = capacidadDisponible;
    }

    public String getIdDeposito() { return idDeposito; }
    public String getNombre() { return nombre; }
    public String getIdNodoLogistico() { return idNodoLogistico; }
    public int getCapacidadMaxima() { return capacidadMaxima; }
    public int getCapacidadDisponible() { return capacidadDisponible; }
    
    // Setter necesario para que el Motor descuente stock
    public void setCapacidadDisponible(int capacidadDisponible) { 
        this.capacidadDisponible = capacidadDisponible; 
    }
}