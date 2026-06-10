package ecommerce.app;

import ecommerce.controller.MotorAsignacionLogistica;
import ecommerce.model.*;
import ecommerce.tda.*;
import ecommerce.tda.impl.*;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("         === SISTEMA LOGISTICO ===                    ");
        System.out.println("======================================================================");

        // Inicializacion de los TDAs
        GestorPedidosTDA gestorPedidos = new GestorPedidosPrioridad();
        GestorInformacionPedidoTDA gestorInfo = new GestorInformacionPedidoDiccionario();
        GestorDepositosTDA gestorDepositos = new GestorDepositosDiccionario();
        ArbolCapacidadDepositosTDA arbolCapacidad = new ArbolCapacidadDepositosAVL();
        RedLogisticaTDA redLogistica = new RedLogisticaGrafo();

        // ==================================================================
        // Setup de depositos
        // Modelo: cada deposito es a la vez almacen y punto de retiro.
        // El stock minimo de seguridad (capacidadMinima) no se puede consumir.
        // ==================================================================
        System.out.println("\n[CONFIGURACION] Cargando depositos e indexando volumenes iniciales...");
        Deposito d1 = new Deposito("D1", "Deposito Ezeiza",      "NODO_EZEIZA",      40, 30, 5);
        Deposito d2 = new Deposito("D2", "Deposito Avellaneda",  "NODO_AVELLANEDA",  60, 40, 5);
        Deposito d3 = new Deposito("D3", "Deposito CABA",        "NODO_CABA",        100, 80, 5);

        gestorDepositos.agregarDeposito(d1);
        gestorDepositos.agregarDeposito(d2);
        gestorDepositos.agregarDeposito(d3);

        arbolCapacidad.insertarDeposito(d1);
        arbolCapacidad.insertarDeposito(d2);
        arbolCapacidad.insertarDeposito(d3);

        // ==================================================================
        // Mapeo del grafo: nodos = depositos, aristas = transferencias internas.
        // Cada conexion guarda (costo en pesos, tiempo en horas).
        // ==================================================================
        redLogistica.agregarNodoLogistico("NODO_EZEIZA");
        redLogistica.agregarNodoLogistico("NODO_AVELLANEDA");
        redLogistica.agregarNodoLogistico("NODO_CABA");

        redLogistica.conectarNodos("NODO_EZEIZA",      "NODO_AVELLANEDA", new ConexionLogistica(3000, 0.5));
        redLogistica.conectarNodos("NODO_EZEIZA",      "NODO_CABA",       new ConexionLogistica(4500, 0.8));
        redLogistica.conectarNodos("NODO_AVELLANEDA",  "NODO_CABA",       new ConexionLogistica(2000, 0.3));

        // ==================================================================
        // Pedidos de prueba.
        // P002 (Premium, destino NODO_CABA) -> deberia ser RETIRO INMEDIATO
        //   porque el deposito de retiro (D3 en CABA) tiene stock suficiente.
        // P001 (Normal, destino NODO_AVELLANEDA) -> deberia ser CONSOLIDACION
        //   porque el deposito de retiro (D2) no alcanza para cubrir todo.
        // ==================================================================
        System.out.println("[E-COMMERCE] Registrando transacciones en la cola prioritaria...");
        Pedido p1 = new Pedido("P001", TipoEnvio.NORMAL,  50, "NODO_AVELLANEDA",
                               new Date(System.currentTimeMillis() - 10000));
        Pedido p2 = new Pedido("P002", TipoEnvio.PREMIUM, 50, "NODO_CABA", new Date());

        gestorInfo.agregarPedido(p1);
        gestorInfo.agregarPedido(p2);

        gestorPedidos.encolarPedido(p1);
        gestorPedidos.encolarPedido(p2);

        System.out.println(" -> Configuracion completada con exito. Listo para simular.");

        // Encendido del Motor Coordinador
        MotorAsignacionLogistica motor = new MotorAsignacionLogistica(
            gestorPedidos, gestorInfo, gestorDepositos, arbolCapacidad, redLogistica);

        // Turno 1: procesa P002 (Premium primero por prioridad).
        motor.procesarSiguientePedido();

        // Turno 2: procesa P001.
        motor.procesarSiguientePedido();

        System.out.println("\n======================================================================");
        System.out.println("[AUDITORIA DE CIERRE] El flujo finalizo de manera limpia.");
        System.out.println("======================================================================");
    }
}
