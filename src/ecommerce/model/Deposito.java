package ecommerce.model;

public class Deposito {
    private String idDeposito;
    private String nombre;
    private String idNodoLogistico;
    private int capacidadMaxima;
    private int capacidadDisponible;

    public Deposito(String idDeposito, String nombre, String idNodoLogistico, int capacidadMaxima) {
        this.idDeposito = idDeposito;
        this.nombre = nombre;
        this.idNodoLogistico = idNodoLogistico;
        this.capacidadMaxima = capacidadMaxima;
        this.capacidadDisponible = capacidadMaxima;
    }

    public String getIdDeposito() { return idDeposito; }
    public String getNombre() { return nombre; }
    public String getIdNodoLogistico() { return idNodoLogistico; }
    public int getCapacidadMaxima() { return capacidadMaxima; }
    public int getCapacidadDisponible() { return capacidadDisponible; }
    public void setCapacidadDisponible(int cap) { this.capacidadDisponible = cap; }
}