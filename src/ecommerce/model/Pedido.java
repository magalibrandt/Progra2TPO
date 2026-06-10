package ecommerce.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Pedido {
    private String idPedido;
    private TipoEnvio tipoEnvio;
    private int capacidadRequerida;
    // destino: idDeposito del deposito de retiro elegido por el cliente al cargar el pedido.
    private String destino;
    private Date fechaIngreso;
    private EstadoPedido estado;
    // Asignaciones generadas por el motor al asignar el pedido.
    // Queda vacia mientras esta PENDIENTE, se completa al pasar a ASIGNADO.
    // Permite revertir las reservas en caso de cancelacion.
    private List<AsignacionDeposito> asignaciones;

    public Pedido(String idPedido, TipoEnvio tipoEnvio, int capacidadRequerida, String destino, Date fechaIngreso) {
        this.idPedido = idPedido;
        this.tipoEnvio = tipoEnvio;
        this.capacidadRequerida = capacidadRequerida;
        this.destino = destino;
        this.fechaIngreso = fechaIngreso;
        this.estado = EstadoPedido.PENDIENTE; // Todo pedido nace como pendiente
        this.asignaciones = new ArrayList<>();
    }

    public String getIdPedido() { return idPedido; }
    public TipoEnvio getTipoEnvio() { return tipoEnvio; }
    public int getCapacidadRequerida() { return capacidadRequerida; }
    public String getDestino() { return destino; }
    public Date getFechaIngreso() { return fechaIngreso; }
    public EstadoPedido getEstado() { return estado; }
    public List<AsignacionDeposito> getAsignaciones() { return asignaciones; }

    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public void setDestino(String destino) { this.destino = destino; }
    public void setAsignaciones(List<AsignacionDeposito> asignaciones) { this.asignaciones = asignaciones; }
}
