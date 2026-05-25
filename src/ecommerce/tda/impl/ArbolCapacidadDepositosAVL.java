package ecommerce.tda.impl;

import ecommerce.model.Deposito;
import ecommerce.tda.ArbolCapacidadDepositosTDA;
import java.util.ArrayList;
import java.util.List;

public class ArbolCapacidadDepositosAVL implements ArbolCapacidadDepositosTDA {
    private class NodoAVL {
        int capacidad;
        List<Deposito> depositos = new ArrayList<>();
        NodoAVL izq, der;
        int altura = 1;
        NodoAVL(int cap) { this.capacidad = cap; }
    }

    private NodoAVL raiz = null;

    private int altura(NodoAVL n) { return n == null ? 0 : n.altura; }
    private int max(int a, int b) { return (a > b) ? a : b; }
    private int balance(NodoAVL n) { return n == null ? 0 : altura(n.izq) - altura(n.der); }

    private NodoAVL rotarDer(NodoAVL y) {
        NodoAVL x = y.izq; NodoAVL T2 = x.der;
        x.der = y; y.izq = T2;
        y.altura = max(altura(y.izq), altura(y.der)) + 1;
        x.altura = max(altura(x.izq), altura(x.der)) + 1;
        return x;
    }

    private NodoAVL rotarIzq(NodoAVL x) {
        NodoAVL y = x.der; NodoAVL T2 = y.izq;
        y.izq = x; x.der = T2;
        x.altura = max(altura(x.izq), altura(x.der)) + 1;
        y.altura = max(altura(y.izq), altura(y.der)) + 1;
        return y;
    }

    @Override
    public void insertarDeposito(Deposito deposito) {
        if (deposito == null) return;
        raiz = insertar(raiz, deposito, deposito.getCapacidadDisponible());
    }

    private NodoAVL insertar(NodoAVL nodo, Deposito d, int cap) {
        if (nodo == null) {
            NodoAVL nuevo = new NodoAVL(cap);
            nuevo.depositos.add(d);
            return nuevo;
        }
        if (cap == nodo.capacidad) { 
            nodo.depositos.add(d); 
            return nodo; 
        }
        if (cap < nodo.capacidad) nodo.izq = insertar(nodo.izq, d, cap);
        else nodo.der = insertar(nodo.der, d, cap);

        nodo.altura = max(altura(nodo.izq), altura(nodo.der)) + 1;
        int bal = balance(nodo);

        if (bal > 1 && cap < nodo.izq.capacidad) return rotarDer(nodo);
        if (bal < -1 && cap > nodo.der.capacidad) return rotarIzq(nodo);
        if (bal > 1 && cap > nodo.izq.capacidad) { nodo.izq = rotarIzq(nodo.izq); return rotarDer(nodo); }
        if (bal < -1 && cap < nodo.der.capacidad) { nodo.der = rotarDer(nodo.der); return rotarIzq(nodo); }
        return nodo;
    }

    @Override
    public void eliminarDeposito(Deposito deposito) {
        if (deposito == null) return;
        // Buscamos eliminarlo barriendo el árbol por su ID operativo
        raiz = eliminarPorId(raiz, deposito.getIdDeposito());
    }

    private NodoAVL eliminarPorId(NodoAVL nodo, String idDeposito) {
        if (nodo == null) return null;

        // Buscamos de forma exhaustiva en el árbol de punteros
        nodo.izq = eliminarPorId(nodo.izq, idDeposito);
        nodo.der = eliminarPorId(nodo.der, idDeposito);

        nodo.depositos.removeIf(x -> x.getIdDeposito().equals(idDeposito));

        // Si la lista de depósitos de este nodo se vació, eliminamos físicamente el nodo AVL
        if (nodo.depositos.isEmpty()) {
            if (nodo.izq == null || nodo.der == null) {
                nodo = (nodo.izq != null) ? nodo.izq : nodo.der;
            } else {
                NodoAVL temp = maximoNodo(nodo.izq);
                nodo.capacidad = temp.capacidad;
                nodo.depositos = new ArrayList<>(temp.depositos);
                nodo.izq = eliminarPorId(nodo.izq, temp.depositos.get(0).getIdDeposito());
            }
        }

        if (nodo == null) return null;

        nodo.altura = max(altura(nodo.izq), altura(nodo.der)) + 1;
        int bal = balance(nodo);

        if (bal > 1 && balance(nodo.izq) >= 0) return rotarDer(nodo);
        if (bal > 1 && balance(nodo.izq) < 0) { nodo.izq = rotarIzq(nodo.izq); return rotarDer(nodo); }
        if (bal < -1 && balance(nodo.der) <= 0) return rotarIzq(nodo);
        if (bal < -1 && balance(nodo.der) > 0) { nodo.der = rotarDer(nodo.der); return rotarIzq(nodo); }
        return nodo;
    }

    private NodoAVL maximoNodo(NodoAVL nodo) {
        NodoAVL act = nodo;
        while (act.der != null) act = act.der;
        return act;
    }

    @Override
    public void actualizarCapacidad(Deposito deposito, int capacidadAnterior) {
        if (deposito == null) return;
        raiz = eliminarPorId(raiz, deposito.getIdDeposito());
        insertarDeposito(deposito);
    }

    @Override
    public List<Deposito> obtenerDepositosConCapacidadDisponible() {
        List<Deposito> res = new ArrayList<>();
        recorrerInOrden(raiz, res);
        return res;
    }

    private void recorrerInOrden(NodoAVL nodo, List<Deposito> res) {
        if (nodo == null) return;
        recorrerInOrden(nodo.izq, res);
        if (nodo.capacidad > 0) {
            res.addAll(nodo.depositos);
        }
        recorrerInOrden(nodo.der, res);
    }

    @Override
    public List<Deposito> buscarDepositosConCapacidadMinima(int capacidadRequerida) {
        List<Deposito> res = new ArrayList<>();
        buscarMinRec(raiz, capacidadRequerida, res);
        return res;
    }

    private void buscarMinRec(NodoAVL nodo, int cap, List<Deposito> res) {
        if (nodo == null) return;
        if (nodo.capacidad >= cap) {
            res.addAll(nodo.depositos);
            buscarMinRec(nodo.izq, cap, res);
        }
        buscarMinRec(nodo.der, cap, res);
    }
}