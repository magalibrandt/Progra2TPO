package ecommerce.tda.impl;

import ecommerce.model.Deposito;
import ecommerce.tda.GestorDepositosTDA;
import java.util.ArrayList;
import java.util.List;

public class GestorDepositosDiccionario implements GestorDepositosTDA {
    private class Nodo {
        String clave;
        Deposito valor;
        Nodo sgte;
    }
    private Nodo raiz = null;

    @Override
    public void agregarDeposito(Deposito deposito) {
        Nodo b = buscar(deposito.getIdDeposito());
        if (b != null) { b.valor = deposito; }
        else {
            Nodo n = new Nodo();
            n.clave = deposito.getIdDeposito();
            n.valor = deposito;
            n.sgte = raiz;
            raiz = n;
        }
    }

    @Override
    public Deposito obtenerDeposito(String idDeposito) {
        Nodo b = buscar(idDeposito);
        return (b != null) ? b.valor : null;
    }

    @Override
    public void actualizarDeposito(Deposito deposito) { agregarDeposito(deposito); }

    @Override
    public boolean tieneCapacidad(String idDeposito, int cantidad) {
        Deposito d = obtenerDeposito(idDeposito);
        if (d == null) return false;
        // Solo se considera disponible la capacidad por encima del stock minimo de seguridad.
        int disponibleUtilizable = d.getCapacidadDisponible() - d.getCapacidadMinima();
        return disponibleUtilizable >= cantidad;
    }

    @Override
    public void reservarCapacidad(String idDeposito, int cantidad) {
        Deposito d = obtenerDeposito(idDeposito);
        if (d == null) return;
        int nuevoStock = d.getCapacidadDisponible() - cantidad;
        if (nuevoStock < d.getCapacidadMinima()) {
            throw new IllegalStateException(
                "La reserva dejaria al deposito " + idDeposito +
                " por debajo del stock minimo de seguridad."
            );
        }
        d.setCapacidadDisponible(nuevoStock);
    }

    @Override
    public void liberarCapacidad(String idDeposito, int cantidad) {
        Deposito d = obtenerDeposito(idDeposito);
        if (d != null) d.setCapacidadDisponible(d.getCapacidadDisponible() + cantidad);
    }

    @Override
    public List<Deposito> obtenerTodos() {
        List<Deposito> lista = new ArrayList<>();
        Nodo aux = raiz;
        while (aux != null) {
            lista.add(aux.valor);
            aux = aux.sgte;
        }
        return lista;
    }

    private Nodo buscar(String id) {
        Nodo aux = raiz;
        while (aux != null && !aux.clave.equals(id)) aux = aux.sgte;
        return aux;
    }
}
