package ecommerce.app;

import ecommerce.controller.MotorAsignacionLogistica;
import ecommerce.model.*;
import ecommerce.tda.*;
import ecommerce.tda.impl.*;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("         === SISTEMA LOGÍSTICO ===                    ");
        System.out.println("======================================================================");

        // Inicialización de componentes lógicos
        GestorPedidosTDA gestorPedidos = new GestorPedidosPrioridad();
        GestorInformacionPedidoTDA gestorInfo = new GestorInformacionPedidoDiccionario();
        GestorDepositosTDA gestorDepositos = new GestorDepositosDiccionario();
        ArbolCapacidadDepositosTDA arbolCapacidad = new ArbolCapacidadDepositosAVL();
        RedLogisticaTDA redLogistica = new RedLogisticaGrafo();

        System.out.println("\n[CONFIGURACIÓN] Cargando depósitos e indexando volúmenes iniciales...");
        Deposito d1 = new Deposito("D1", "Depósito Ezeiza", "NODO_EZEIZA", 100, 20);
        Deposito d2 = new Deposito("D2", "Depósito Avellaneda", "NODO_AVELLANEDA", 100, 30);
        Deposito d3 = new Deposito("D3", "Depósito CABA", "NODO_CABA", 200, 60);

        gestorDepositos.agregarDeposito(d1);
        gestorDepositos.agregarDeposito(d2);
        gestorDepositos.agregarDeposito(d3);

        arbolCapacidad.insertarDeposito(d1);
        arbolCapacidad.insertarDeposito(d2);
        arbolCapacidad.insertarDeposito(d3);

        // Mapeo e interconexión del Grafo
        redLogistica.agregarNodoLogistico("NODO_EZEIZA");
        redLogistica.agregarNodoLogistico("NODO_AVELLANEDA");
        redLogistica.agregarNodoLogistico("NODO_CABA");
        redLogistica.agregarNodoLogistico("DESTINO_X");

        redLogistica.conectarNodos("NODO_EZEIZA", "DESTINO_X", new ConexionLogistica(5000, 1.0));
        redLogistica.conectarNodos("NODO_AVELLANEDA", "DESTINO_X", new ConexionLogistica(8000, 1.2));
        redLogistica.conectarNodos("NODO_CABA", "DESTINO_X", new ConexionLogistica(35000, 4.0));
        
        System.out.println("[E-COMMERCE] Registrando transacciones en la cola prioritaria...");
        Pedido p1 = new Pedido("P001", TipoEnvio.NORMAL, 50, "DESTINO_X", new Date(System.currentTimeMillis() - 10000));
        Pedido p2 = new Pedido("P002", TipoEnvio.PREMIUM, 50, "DESTINO_X", new Date());

        gestorInfo.agregarPedido(p1);
        gestorInfo.agregarPedido(p2);

        gestorPedidos.encolarPedido(p1);
        gestorPedidos.encolarPedido(p2);

        System.out.println(" -> Configuración completada con éxito. Listo para simular.");

        // Encendido del Motor Coordinador
        MotorAsignacionLogistica motor = new MotorAsignacionLogistica(gestorPedidos, gestorInfo, gestorDepositos, arbolCapacidad, redLogistica);

        // Turno 1 (Procesa P002 por prioridad Premium)
        motor.procesarSiguientePedido();

        // Turno 2 (Procesa P001)
        motor.procesarSiguientePedido();

        System.out.println("\n======================================================================");
        System.out.println("[AUDITORÍA DE CIERRE] El flujo finalizó de manera limpia.");
        System.out.println("======================================================================");
    }
}