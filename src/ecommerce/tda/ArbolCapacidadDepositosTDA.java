package ecommerce.tda;

import ecommerce.model.Deposito;
import java.util.List;

public interface ArbolCapacidadDepositosTDA {
    void insertarDeposito(Deposito deposito);
    void eliminarDeposito(Deposito deposito);
    void actualizarCapacidad(Deposito deposito, int capacidadAnterior);
    List<Deposito> obtenerDepositosConCapacidadDisponible();
    List<Deposito> buscarDepositosConCapacidadMinima(int capacidadRequerida);
}