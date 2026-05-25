package ecommerce.app;

import ecommerce.controller.MotorAsignacionLogistica;
import ecommerce.model.*;
import ecommerce.tda.*;
import ecommerce.tda.impl.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("         === SISTEMA LOGÍSTICO ===");
        System.out.println("======================================================================");
        GestorPedidosTDA gestorPedidos = new GestorPedidosPrioridad();
        GestorInformacionPedidoTDA gestorInfo = new GestorInformacionPedidoDiccionario();
        GestorDepositosTDA gestorDepositos = new GestorDepositosDiccionario();
        ArbolCapacidadDepositosTDA arbolCapacidad = new ArbolCapacidadDepositosAVL();
        RedLogisticaTDA redLogistica = new RedLogisticaGrafo();

        System.out.println("\n[CONFIGURACIÓN] Cargando depósitos e indexando volúmenes iniciales...");
        
        
        Deposito d1 = new Deposito("D1", "Depósito Ezeiza", "NODO_EZEIZA", 100);
        d1.setCapacidadDisponible(90);
        Deposito d2 = new Deposito("D2", "Depósito Avellaneda", "NODO_AVELLANEDA", 100);
        d2.setCapacidadDisponible(80);
        Deposito d3 = new Deposito("D3", "Depósito CABA", "NODO_CABA", 100);
        d3.setCapacidadDisponible(60);

        gestorDepositos.agregarDeposito(d1);
        gestorDepositos.agregarDeposito(d2);
        gestorDepositos.agregarDeposito(d3);

        arbolCapacidad.insertarDeposito(d1);
        arbolCapacidad.insertarDeposito(d2);
        arbolCapacidad.insertarDeposito(d3);

        
        redLogistica.conectarNodos("NODO_EZEIZA", "DESTINO_X", new ConexionLogistica(5, 500, 1000, 1.0, 500));
        redLogistica.conectarNodos("NODO_AVELLANEDA", "DESTINO_X", new ConexionLogistica(10, 700, 1500, 1.2, 700));
        redLogistica.conectarNodos("NODO_CABA", "DESTINO_X", new ConexionLogistica(50, 300, 1000, 4.0, 400));

        System.out.println("[E-COMMERCE] Registrando transacciones en la cola prioritaria...");
        Pedido pNormal = new Pedido("P001", TipoEnvio.NORMAL, 50, "DESTINO_X");
        Pedido pPremium = new Pedido("P002", TipoEnvio.PREMIUM, 50, "DESTINO_X");

        gestorPedidos.encolarPedido(pNormal);
        gestorPedidos.encolarPedido(pPremium); 
        gestorInfo.agregarPedido(pNormal);
        gestorInfo.agregarPedido(pPremium);

        System.out.println(" -> Configuración completada con éxito. Listo para simular.");

        MotorAsignacionLogistica motor = new MotorAsignacionLogistica(gestorPedidos, gestorInfo, gestorDepositos, arbolCapacidad, redLogistica);

        List<AsignacionDeposito> asig1 = motor.procesarSiguientePedido();
        for (AsignacionDeposito asig : asig1) {
            System.out.println("  * " + asig);
        }

        List<AsignacionDeposito> asig2 = motor.procesarSiguientePedido();
        for (AsignacionDeposito asig : asig2) {
            System.out.println("  * " + asig);
        }

        System.out.println("\n======================================================================");
        System.out.println("[AUDITORÍA DE CIERRE] El flujo finalizó de manera limpia.");
        System.out.println("======================================================================");
    }
}