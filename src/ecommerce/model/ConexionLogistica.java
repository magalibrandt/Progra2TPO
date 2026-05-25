package ecommerce.model;

public class ConexionLogistica {
    public double distanciaKm;
    public double costoPeajes;
    public double costoCombustible;
    public double tiempoEstimadoHoras;
    public double costoOperativo;

    public ConexionLogistica(double dist, double peajes, double comb, double tiempo, double op) {
        this.distanciaKm = dist;
        this.costoPeajes = peajes;
        this.costoCombustible = comb;
        this.tiempoEstimadoHoras = tiempo;
        this.costoOperativo = op;
    }

    public double calcularPeso(TipoEnvio tipo) {
        if (tipo == TipoEnvio.NORMAL) {
            return costoCombustible + costoPeajes + costoOperativo + (distanciaKm * 100.0);
        }
        return (tiempoEstimadoHoras * 1000.0) + ((costoCombustible + costoPeajes) * 0.10);
    }
}