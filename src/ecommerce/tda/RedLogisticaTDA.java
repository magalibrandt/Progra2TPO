package ecommerce.tda;

import ecommerce.model.ConexionLogistica;

public interface RedLogisticaTDA {
    void agregarNodoLogistico(String idNodo);

    void conectarNodos(String idOrigen, String idDestino, ConexionLogistica conexion);

    double obtenerCostoRuta(String idOrigen, String idDestino);

    double obtenerTiempoRuta(String idOrigen, String idDestino);

    boolean existeConexion(String idOrigen, String idDestino);
}