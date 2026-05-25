package ecommerce.tda;

import ecommerce.model.Pedido;

public interface GestorInformacionPedidoTDA {
    void agregarPedido(Pedido pedido);
    Pedido obtenerPedido(String idPedido);
    void actualizarPedido(Pedido pedido);
    void eliminarPedido(String idPedido);
    boolean existePedido(String idPedido);
}