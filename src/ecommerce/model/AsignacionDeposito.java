package ecommerce.model;

public class AsignacionDeposito {
    private String idDeposito;
    private int cantidadAsignada;
    private double costoEstimado;

    public AsignacionDeposito(String idDeposito, int cantidadAsignada, double costoEstimado) {
        this.idDeposito = idDeposito;
        this.cantidadAsignada = cantidadAsignada;
        this.costoEstimado = costoEstimado;
    }

    @Override
    public String toString() {
        return "Asignacion -> Deposito: " + idDeposito + " | Cantidad: " + cantidadAsignada + " | Peso Logistico: " + costoEstimado;
    }
}