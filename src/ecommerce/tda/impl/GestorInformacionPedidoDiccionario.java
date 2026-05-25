package ecommerce.tda.impl;

import ecommerce.model.Pedido;
import ecommerce.tda.GestorInformacionPedidoTDA;

public class GestorInformacionPedidoDiccionario implements GestorInformacionPedidoTDA {
    private class Nodo {
        String clave;
        Pedido valor;
        Nodo sgte;
    }
    private Nodo raiz = null;

    @Override
    public void agregarPedido(Pedido pedido) {
        Nodo b = buscar(pedido.getIdPedido());
        if (b != null) { b.valor = pedido; } 
        else {
            Nodo n = new Nodo();
            n.clave = pedido.getIdPedido();
            n.valor = pedido;
            n.sgte = raiz;
            raiz = n;
        }
    }

    @Override
    public Pedido obtenerPedido(String idPedido) {
        Nodo b = buscar(idPedido);
        return (b != null) ? b.valor : null;
    }

    @Override
    public void actualizarPedido(Pedido pedido) { agregarPedido(pedido); }

    @Override
    public void eliminarPedido(String idPedido) {
        if (raiz == null) return;
        if (raiz.clave.equals(idPedido)) { raiz = raiz.sgte; return; }
        Nodo ant = raiz; Nodo act = raiz.sgte;
        while (act != null && !act.clave.equals(idPedido)) { ant = act; act = act.sgte; }
        if (act != null) ant.sgte = act.sgte;
    }

    @Override
    public boolean existePedido(String idPedido) { return buscar(idPedido) != null; }

    private Nodo buscar(String clave) {
        Nodo aux = raiz;
        while (aux != null && !aux.clave.equals(clave)) aux = aux.sgte;
        return aux;
    }
}