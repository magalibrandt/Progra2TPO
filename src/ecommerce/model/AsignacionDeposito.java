package ecommerce.model;

public class AsignacionDeposito {
    private String idDeposito;
    private int cantidad;
    private double costoRuta;

    public AsignacionDeposito(String idDeposito, int cantidad, double costoRuta) {
        this.idDeposito = idDeposito;
        this.cantidad = cantidad;
        this.costoRuta = costoRuta;
    }

    public String getIdDeposito() { return idDeposito; }
    public int getCantidad() { return cantidad; }
    public double getCostoRuta() { return costoRuta; }
}