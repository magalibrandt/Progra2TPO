package ecommerce.tda;

import ecommerce.model.ConexionLogistica;
import ecommerce.model.TipoEnvio;

public interface RedLogisticaTDA {
    void agregarNodoLogistico(String idNodo);
    void conectarNodos(String idOrigen, String idDestino, ConexionLogistica conexion);
    double obtenerCostoRuta(String idOrigen, String idDestino, TipoEnvio tipoEnvio);
}
