package ecommerce.tda;

import ecommerce.model.Deposito;
import java.util.List;

public interface GestorDepositosTDA {
    void agregarDeposito(Deposito deposito);
    Deposito obtenerDeposito(String idDeposito);
    void actualizarDeposito(Deposito deposito);
    boolean tieneCapacidad(String idDeposito, int cantidad);
    void reservarCapacidad(String idDeposito, int cantidad);
    void liberarCapacidad(String idDeposito, int cantidad);
    List<Deposito> obtenerTodos();
}