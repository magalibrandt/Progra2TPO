package ecommerce.tda;

import ecommerce.model.Pedido;

public interface GestorPedidosTDA {
    void encolarPedido(Pedido pedido);
    Pedido desencolarPedido();
    Pedido obtenerPedidoPrioritario();
    boolean estaVacio();
}