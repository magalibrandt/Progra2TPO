package ecommerce.model;

public class ConexionLogistica {
    private double costo;
    private double tiempoHoras;

    public ConexionLogistica(double costo, double tiempoHoras) {
        this.costo = costo;
        this.tiempoHoras = tiempoHoras;
    }

    public double getCosto() {
        return costo;
    }

    public double getTiempoHoras() {
        return tiempoHoras;
    }
}