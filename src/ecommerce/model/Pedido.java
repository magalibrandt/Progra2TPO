package ecommerce.model;

import java.util.Date;

public class Pedido {
    private String idPedido;
    private TipoEnvio tipoEnvio;
    private int capacidadRequerida;
    private String destino;
    private Date fechaIngreso;
    private EstadoPedido estado;

    public Pedido(String idPedido, TipoEnvio tipoEnvio, int capacidadRequerida, String destino, Date fechaIngreso) {
        this.idPedido = idPedido;
        this.tipoEnvio = tipoEnvio;
        this.capacidadRequerida = capacidadRequerida;
        this.destino = destino;
        this.fechaIngreso = fechaIngreso;
        this.estado = EstadoPedido.PENDIENTE; // Todo pedido nace como pendiente
    }

    public String getIdPedido() { return idPedido; }
    public TipoEnvio getTipoEnvio() { return tipoEnvio; }
    public int getCapacidadRequerida() { return capacidadRequerida; }
    public String getDestino() { return destino; }
    public Date getFechaIngreso() { return fechaIngreso; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
}