// Paquete: ecommerce.tda.impl | Clase: RedLogisticaGrafo.java
package ecommerce.tda.impl;

import ecommerce.model.ConexionLogistica;
import ecommerce.model.TipoEnvio;
import ecommerce.tda.RedLogisticaTDA;

public class RedLogisticaGrafo implements RedLogisticaTDA {

    // 1. Definición de los Nodos del Grafo Dinámico (Lista de Adyacencia)
    private class NodoGrafo {
        String idNodo;        // Vértice (Ej: "NODO_EZEIZA")
        NodoArista arista;    // Puntero a la lista de rutas que SALEN de este nodo
        NodoGrafo sigNodo;    // Puntero a la siguiente ciudad en la lista principal
    }

    private class NodoArista {
        ConexionLogistica conexion; // El peso multicriterio (distancia, peajes, etc.)
        NodoGrafo nodoDestino;      // Hacia dónde va esta ruta
        NodoArista sigArista;       // Siguiente ruta que sale del mismo origen
    }

    private NodoGrafo origen; // Puntero inicial de la lista de vértices

    @Override
    public void agregarNodoLogistico(String idNodo) {
        // No permitimos duplicados
        if (buscarNodo(idNodo) != null) return;

        NodoGrafo nuevo = new NodoGrafo();
        nuevo.idNodo = idNodo;
        nuevo.arista = null;
        nuevo.sigNodo = origen; // Insertamos al principio para O(1)
        origen = nuevo;
    }

    // Método auxiliar privado para localizar un vértice en memoria
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

        // Creamos la arista dinámica
        NodoArista nuevaArista = new NodoArista();
        nuevaArista.conexion = conexion;
        nuevaArista.nodoDestino = nodoD;
        
        // La insertamos al frente de la lista de aristas del nodo Origen
        nuevaArista.sigArista = nodoO.arista;
        nodoO.arista = nuevaArista;

        // Si el grafo es NO DIRIGIDO (las rutas son ida y vuelta), tenés que hacer la conexión simétrica:
        NodoArista aristaInversa = new NodoArista();
        aristaInversa.conexion = conexion; // Asumiendo que el costo es el mismo al revés
        aristaInversa.nodoDestino = nodoO;
        aristaInversa.sigArista = nodoD.arista;
        nodoD.arista = aristaInversa;
    }

    @Override
    public double obtenerCostoRuta(String idOrigen, String idDestino, TipoEnvio tipoEnvio) {
        NodoGrafo nodoO = buscarNodo(idOrigen);
        if (nodoO == null) return Double.MAX_VALUE;

        // Recorremos la lista de aristas (rutas) de la ciudad de origen
        NodoArista auxArista = nodoO.arista;
        while (auxArista != null) {
            // Si la ruta va al destino que buscamos, calculamos el peso
            if (auxArista.nodoDestino.idNodo.equals(idDestino)) {
                return auxArista.conexion.calcularPeso(tipoEnvio);
            }
            auxArista = auxArista.sigArista;
        }

        // Si terminó el bucle y no retornó, es porque no hay conexión directa
        return Double.MAX_VALUE;
    }
}