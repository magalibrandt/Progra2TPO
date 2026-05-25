package ecommerce.model;

import java.util.Date;

public class Pedido {
    private String idPedido;
    private TipoEnvio tipoEnvio;
    private EstadoPedido estado;
    private int capacidadRequerida;
    private String destino;
    private Date fechaIngreso;

    public Pedido(String idPedido, TipoEnvio tipoEnvio, int capacidadRequerida, String destino) {
        this.idPedido = idPedido;
        this.tipoEnvio = tipoEnvio;
        this.capacidadRequerida = capacidadRequerida;
        this.destino = destino;
        this.estado = EstadoPedido.PENDIENTE;
        this.fechaIngreso = new Date();
    }

    public String getIdPedido() { return idPedido; }
    public TipoEnvio getTipoEnvio() { return tipoEnvio; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public int getCapacidadRequerida() { return capacidadRequerida; }
    public String getDestino() { return destino; }
    public Date getFechaIngreso() { return fechaIngreso; }
}