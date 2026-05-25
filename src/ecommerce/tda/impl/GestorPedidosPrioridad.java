package ecommerce.tda.impl;

import ecommerce.model.Pedido;
import ecommerce.model.TipoEnvio;
import ecommerce.tda.GestorPedidosTDA;

public class GestorPedidosPrioridad implements GestorPedidosTDA {
    private class Nodo {
        Pedido pedido;
        Nodo sgte;
    }
    private Nodo raiz = null;

    @Override
    public void encolarPedido(Pedido pedido) {
        Nodo nuevo = new Nodo();
        nuevo.pedido = pedido;

        if (raiz == null || comparar(nuevo.pedido, raiz.pedido) < 0) {
            nuevo.sgte = raiz;
            raiz = nuevo;
        } else {
            Nodo ant = raiz;
            Nodo act = raiz.sgte;
            while (act != null && comparar(nuevo.pedido, act.pedido) >= 0) {
                ant = act;
                act = act.sgte;
            }
            nuevo.sgte = act;
            ant.sgte = nuevo;
        }
    }

    private int comparar(Pedido p1, Pedido p2) {
        if (p1.getTipoEnvio() == TipoEnvio.PREMIUM && p2.getTipoEnvio() == TipoEnvio.NORMAL) return -1;
        if (p1.getTipoEnvio() == TipoEnvio.NORMAL && p2.getTipoEnvio() == TipoEnvio.PREMIUM) return 1;
        int compFecha = p1.getFechaIngreso().compareTo(p2.getFechaIngreso());
        if (compFecha != 0) return compFecha;
        return p1.getIdPedido().compareTo(p2.getIdPedido());
    }

    @Override
    public Pedido desencolarPedido() {
        if (raiz == null) return null;
        Pedido p = raiz.pedido;
        raiz = raiz.sgte;
        return p;
    }

    @Override
    public Pedido obtenerPedidoPrioritario() { return (raiz != null) ? raiz.pedido : null; }

    @Override
    public boolean estaVacio() { return raiz == null; }
}