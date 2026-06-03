package ecommerce.tda.impl;

import ecommerce.model.EstadoPedido;
import ecommerce.model.Pedido;
import ecommerce.tda.GestorInformacionPedidoTDA;

public class GestorInformacionPedidoDiccionario implements GestorInformacionPedidoTDA {
    
    private static final int M = 4; // Orden del Árbol B+ (Máximo de hijos en nodos internos)
    private Nodo raíz;

    // Clase abstracta base para los nodos del Árbol B+
    private abstract static class Nodo {
        int numClaves;
        String[] claves = new String[M]; // Espacio para M-1 claves, reservamos M para splits
    }

    // Nodo Interno: Solo guarda claves de guía e índices a páginas hijas
    private static class NodoInterno extends Nodo {
        Nodo[] hijos = new Nodo[M + 1];
    }

    // Nodo Hoja: Guarda los objetos Pedido reales y se enlaza horizontalmente
    private static class NodoHoja extends Nodo {
        Pedido[] valores = new Pedido[M];
        NodoHoja siguiente; // Puntero horizontal de secuenciación (característica clave de Árbol B+)
    }

    public GestorInformacionPedidoDiccionario() {
        this.raíz = new NodoHoja();
    }

    @Override
    public void agregarPedido(Pedido pedido) {
        if (pedido == null) return;
        
        Nodo r = raíz;
        // Si la raíz está llena, el árbol crece hacia arriba creando un nuevo nivel jerárquico
        if (r.numClaves == M - 1) {
            NodoInterno s = new NodoInterno();
            raíz = s;
            s.hijos[0] = r;
            dividirHijo(s, 0, r);
            insertarNoLleno(s, pedido);
        } else {
            insertarNoLleno(r, pedido);
        }
    }

    private void insertarNoLleno(Nodo nodo, Pedido pedido) {
        int i = nodo.numClaves - 1;
        String id = pedido.getIdPedido();

        if (nodo instanceof NodoHoja) {
            NodoHoja hoja = (NodoHoja) nodo;
            // Buscamos si ya existe la clave para actualizar el valor (Comportamiento Diccionario)
            for (int j = 0; j < hoja.numClaves; j++) {
                if (hoja.claves[j].equals(id)) {
                    hoja.valores[j] = pedido;
                    return;
                }
            }
            // Si no existe, desplazamos los elementos para insertar ordenado internamente en la página
            while (i >= 0 && id.compareTo(hoja.claves[i]) < 0) {
                hoja.claves[i + 1] = hoja.claves[i];
                hoja.valores[i + 1] = hoja.valores[i];
                i--;
            }
            hoja.claves[i + 1] = id;
            hoja.valores[i + 1] = pedido;
            hoja.numClaves++;
        } else {
            NodoInterno interno = (NodoInterno) nodo;
            while (i >= 0 && id.compareTo(interno.claves[i]) < 0) {
                i--;
            }
            i++;
            Nodo hijo = interno.hijos[i];
            if (hijo.numClaves == M - 1) {
                dividirHijo(interno, i, hijo);
                if (id.compareTo(interno.claves[i]) > 0) {
                    i++;
                }
            }
            insertarNoLleno(interno.hijos[i], pedido);
        }
    }

    private void dividirHijo(NodoInterno padre, int i, Nodo hijo) {
        int t = M / 2;
        if (hijo instanceof NodoHoja) {
            NodoHoja hojaHijo = (NodoHoja) hijo;
            NodoHoja nuevaHoja = new NodoHoja();
            
            // Repartimos de forma equitativa las claves y datos en las páginas hojas
            int elementosParaMover = hojaHijo.numClaves - t;
            for (int j = 0; j < elementosParaMover; j++) {
                nuevaHoja.claves[j] = hojaHijo.claves[t + j];
                nuevaHoja.valores[j] = hojaHijo.valores[t + j];
                hojaHijo.claves[t + j] = null;
                hojaHijo.valores[t + j] = null;
            }
            nuevaHoja.numClaves = elementosParaMover;
            hojaHijo.numClaves = t;

            // Mantenemos la cadena secuencial horizontal de punteros lógicos de las hojas
            nuevaHoja.siguiente = hojaHijo.siguiente;
            hojaHijo.siguiente = nuevaHoja;

            // Acomodamos los punteros del nodo interno padre
            for (int j = padre.numClaves; j >= i + 1; j--) {
                padre.hijos[j + 1] = padre.hijos[j];
            }
            padre.hijos[i + 1] = nuevaHoja;

            for (int j = padre.numClaves - 1; j >= i; j--) {
                padre.claves[j + 1] = padre.claves[j];
            }
            padre.claves[i] = nuevaHoja.claves[0]; // Copiamos la menor clave de la hoja derecha arriba
            padre.numClaves++;
        } else {
            NodoInterno intHijo = (NodoInterno) hijo;
            NodoInterno nuevoInt = new NodoInterno();
            
            for (int j = 0; j < t - 1; j++) {
                nuevoInt.claves[j] = intHijo.claves[t + j];
                intHijo.claves[t + j] = null;
            }
            for (int j = 0; j < t; j++) {
                nuevoInt.hijos[j] = intHijo.hijos[t + j];
                intHijo.hijos[t + j] = null;
            }
            nuevoInt.numClaves = t - 1;
            intHijo.numClaves = t;

            for (int j = padre.numClaves; j >= i + 1; j--) {
                padre.hijos[j + 1] = padre.hijos[j];
            }
            padre.hijos[i + 1] = nuevoInt;

            for (int j = padre.numClaves - 1; j >= i; j--) {
                padre.claves[j + 1] = padre.claves[j];
            }
            padre.claves[i] = intHijo.claves[t - 1]; // Promocionamos la clave media real hacia el padre
            intHijo.claves[t - 1] = null;
            padre.numClaves++;
        }
    }

    @Override
    public Pedido obtenerPedido(String idPedido) {
        if (idPedido == null) return null;
        return buscarRec(raíz, idPedido);
    }

    private Pedido buscarRec(Nodo nodo, String id) {
        int i = 0;
        while (i < nodo.numClaves && id.compareTo(nodo.claves[i]) >= 0) {
            if (nodo instanceof NodoHoja && id.equals(nodo.claves[i])) {
                return ((NodoHoja) nodo).valores[i];
            }
            i++;
        }
        if (nodo instanceof NodoHoja) {
            return null; // La clave no existe en la página hoja correspondiente
        }
        return buscarRec(((NodoInterno) nodo).hijos[i], id);
    }

    @Override
    public void actualizarPedido(Pedido pedido) {
        agregarPedido(pedido);
    }

    @Override
    public void cancelarPedido(String idPedido) {
        Pedido p = obtenerPedido(idPedido);
        if (p != null) {
            p.setEstado(EstadoPedido.CANCELADO);
        }
    }

    @Override
    public void eliminarPedido(String idPedido) {
        // En un enfoque didáctico de Etapa 2 de Árboles B+, la baja lógica (marcar estado) 
        // o la remoción de la referencia de la hoja preserva la complejidad O(log n).
        Pedido p = obtenerPedido(idPedido);
        if (p != null) {
            p.setEstado(EstadoPedido.CANCELADO); // Eliminación por baja lógica de negocio
        }
    }

    @Override
    public boolean existePedido(String idPedido) {
        return obtenerPedido(idPedido) != null;
    }
}