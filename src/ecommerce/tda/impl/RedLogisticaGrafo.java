package ecommerce.tda.impl;

import ecommerce.model.ConexionLogistica;
import ecommerce.tda.RedLogisticaTDA;

public class RedLogisticaGrafo implements RedLogisticaTDA {

    // Grafo implementado con lista de adyacencia.
    // Cada NodoGrafo representa un punto logístico.
    private class NodoGrafo {
        String idNodo;
        NodoArista arista;
        NodoGrafo sigNodo;
    }

    // Cada NodoArista representa una ruta saliente desde un nodo origen.
    // La conexión guarda únicamente costo y tiempo de viaje.
    private class NodoArista {
        ConexionLogistica conexion;
        NodoGrafo nodoDestino;
        NodoArista sigArista;
    }

    private NodoGrafo origen;

    @Override
    public void agregarNodoLogistico(String idNodo) {
        if (buscarNodo(idNodo) != null) return;

        NodoGrafo nuevo = new NodoGrafo();
        nuevo.idNodo = idNodo;
        nuevo.arista = null;
        nuevo.sigNodo = origen;
        origen = nuevo;
    }

    private NodoGrafo buscarNodo(String id) {
        NodoGrafo aux = origen;

        while (aux != null && !aux.idNodo.equals(id)) {
            aux = aux.sigNodo;
        }

        return aux;
    }

    @Override
    public void conectarNodos(String idOrigen, String idDestino, ConexionLogistica conexion) {
        NodoGrafo nodoO = buscarNodo(idOrigen);
        NodoGrafo nodoD = buscarNodo(idDestino);

        if (nodoO == null || nodoD == null) {
            throw new IllegalArgumentException("Ambos nodos deben existir para crear una conexión.");
        }

        agregarArista(nodoO, nodoD, conexion);

        // El modelo se mantiene no dirigido: si existe ruta de ida, también existe de vuelta.
        // Si quieren que sea dirigido, borren esta línea.
        agregarArista(nodoD, nodoO, conexion);
    }

    private void agregarArista(NodoGrafo origen, NodoGrafo destino, ConexionLogistica conexion) {
        NodoArista nuevaArista = new NodoArista();
        nuevaArista.conexion = conexion;
        nuevaArista.nodoDestino = destino;
        nuevaArista.sigArista = origen.arista;
        origen.arista = nuevaArista;
    }

    @Override
    public boolean existeConexion(String idOrigen, String idDestino) {
        return buscarArista(idOrigen, idDestino) != null;
    }

    @Override
    public double obtenerCostoRuta(String idOrigen, String idDestino) {
        NodoArista arista = buscarArista(idOrigen, idDestino);

        if (arista == null) {
            return Double.MAX_VALUE;
        }

        return arista.conexion.getCosto();
    }

    @Override
    public double obtenerTiempoRuta(String idOrigen, String idDestino) {
        NodoArista arista = buscarArista(idOrigen, idDestino);

        if (arista == null) {
            return Double.MAX_VALUE;
        }

        return arista.conexion.getTiempoHoras();
    }

    private NodoArista buscarArista(String idOrigen, String idDestino) {
        NodoGrafo nodoO = buscarNodo(idOrigen);

        if (nodoO == null) {
            return null;
        }

        NodoArista auxArista = nodoO.arista;

        while (auxArista != null) {
            if (auxArista.nodoDestino.idNodo.equals(idDestino)) {
                return auxArista;
            }

            auxArista = auxArista.sigArista;
        }

        return null;
    }
}