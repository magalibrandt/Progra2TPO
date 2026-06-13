package ecommerce.app;

import ecommerce.controller.MotorAsignacionLogistica;
import ecommerce.model.*;
import ecommerce.tda.*;
import ecommerce.tda.impl.*;
import java.util.Date;

public class Main {

    // TDAs e instancia del motor compartidos por todos los escenarios de prueba
    private static GestorPedidosTDA gestorPedidos;
    private static GestorInformacionPedidoTDA gestorInfo;
    private static GestorDepositosTDA gestorDepositos;
    private static ArbolCapacidadDepositosTDA arbolCapacidad;
    private static RedLogisticaTDA redLogistica;
    private static MotorAsignacionLogistica motor;

    public static void main(String[] args) {
        printBanner("SISTEMA LOGISTICO - DEMOSTRACION COMPLETA");

        // ==========================================================
        // CONFIGURACION INICIAL
        // ==========================================================
        printSeparator("CONFIGURACION INICIAL");
        inicializarTDAs();
        cargarDepositos();
        cargarConexionesGrafo();
        printEstadoDepositos("Estado inicial de capacidades");

        // ==========================================================
        // TEST 1: Retiro en el dia (envio Premium)
        // El cliente retira en CABA y D3 (CABA) tiene stock suficiente:
        // no hay transferencia y el pedido queda listo para retiro inmediato.
        // ==========================================================
        printSeparator("TEST 1 - RETIRO EN EL DIA (Premium)");
        Pedido p001 = new Pedido("P001", TipoEnvio.PREMIUM, 50, "NODO_CABA", new Date());
        registrarPedido(p001);
        motor.procesarSiguientePedido();
        printEstadoDepositos("Estado tras procesar P001");

        // ==========================================================
        // TEST 2: Consolidacion (envio Normal)
        // El cliente retira en Avellaneda. D2 (Avellaneda) tiene capacidad utilizable
        // limitada por el stock minimo. El motor consolida desde D2 + D3 priorizando
        // el menor costo de transferencia (criterio Normal).
        // ==========================================================
        printSeparator("TEST 2 - CONSOLIDACION (Normal)");
        Pedido p002 = new Pedido("P002", TipoEnvio.NORMAL, 50, "NODO_AVELLANEDA",
                new Date(System.currentTimeMillis() + 1000));
        registrarPedido(p002);
        motor.procesarSiguientePedido();
        printEstadoDepositos("Estado tras procesar P002");

        // ==========================================================
        // TEST 3: Cancelacion de un pedido ya asignado
        // P001 estaba ASIGNADO con 50 unidades reservadas en D3.
        // Al cancelar, el sistema libera la capacidad y actualiza el AVL.
        // ==========================================================
        printSeparator("TEST 3 - CANCELACION DE UN PEDIDO ASIGNADO");
        System.out.println("[CLIENTE] Solicita cancelar el pedido P001.");
        motor.cancelarPedido("P001");
        printEstadoDepositos("Estado tras cancelar P001 (D3 recupera las 50 unidades)");

        // ==========================================================
        // TEST 4: Validacion de stock minimo de seguridad
        // Se intenta reservar manualmente una cantidad que dejaria al deposito
        // por debajo del piso minimo. El GestorDepositos lo rechaza con excepcion.
        // ==========================================================
        printSeparator("TEST 4 - VALIDACION DE STOCK MINIMO");
        System.out.println("[OPERADOR] Intenta reservar 28 unidades de D1.");
        System.out.println("           D1 tiene 30 disponibles, 5 de minimo => solo 25 utilizables.");
        try {
            gestorDepositos.reservarCapacidad("D1", 28);
            System.out.println("[ERROR] La reserva no deberia haberse aceptado.");
        } catch (IllegalStateException e) {
            System.out.println("[REGLA DE NEGOCIO] Reserva rechazada -> " + e.getMessage());
        }
        printEstadoDepositos("Estado tras intento fallido (D1 sin cambios)");

        // ==========================================================
        // CIERRE
        // ==========================================================
        printSeparator("FIN DE LA DEMOSTRACION");
        System.out.println("Se verificaron en orden:");
        System.out.println("  1. Retiro en el dia cuando el deposito de retiro cubre todo.");
        System.out.println("  2. Consolidacion desde varios depositos minimizando costo.");
        System.out.println("  3. Cancelacion con devolucion de capacidades y actualizacion del AVL.");
        System.out.println("  4. Rechazo automatico de reservas que violarian el stock minimo.");
        printBanner("");
    }

    // ==========================================================
    // Helpers de inicializacion
    // ==========================================================

    private static void inicializarTDAs() {
        gestorPedidos = new GestorPedidosPrioridad();
        gestorInfo = new GestorInformacionPedidoDiccionario();
        gestorDepositos = new GestorDepositosDiccionario();
        arbolCapacidad = new ArbolCapacidadDepositosAVL();
        redLogistica = new RedLogisticaGrafo();
        motor = new MotorAsignacionLogistica(
                gestorPedidos, gestorInfo, gestorDepositos, arbolCapacidad, redLogistica);
    }

    private static void cargarDepositos() {
        // Parametros: (idDeposito, nombre, idNodoLogistico, capMax, capDisponible, capMinima)
        Deposito d1 = new Deposito("D1", "Deposito Ezeiza",     "NODO_EZEIZA",     40, 30, 5);
        Deposito d2 = new Deposito("D2", "Deposito Avellaneda", "NODO_AVELLANEDA", 60, 40, 5);
        Deposito d3 = new Deposito("D3", "Deposito CABA",       "NODO_CABA",       100, 80, 5);

        gestorDepositos.agregarDeposito(d1);
        gestorDepositos.agregarDeposito(d2);
        gestorDepositos.agregarDeposito(d3);

        arbolCapacidad.insertarDeposito(d1);
        arbolCapacidad.insertarDeposito(d2);
        arbolCapacidad.insertarDeposito(d3);

        System.out.println("Depositos creados:");
        System.out.println("  D1 - Deposito Ezeiza      (cap 40, disp 30, min 5)");
        System.out.println("  D2 - Deposito Avellaneda  (cap 60, disp 40, min 5)");
        System.out.println("  D3 - Deposito CABA        (cap 100, disp 80, min 5)");
    }

    private static void cargarConexionesGrafo() {
        redLogistica.agregarNodoLogistico("NODO_EZEIZA");
        redLogistica.agregarNodoLogistico("NODO_AVELLANEDA");
        redLogistica.agregarNodoLogistico("NODO_CABA");

        redLogistica.conectarNodos("NODO_EZEIZA",     "NODO_AVELLANEDA", new ConexionLogistica(3000, 0.5));
        redLogistica.conectarNodos("NODO_EZEIZA",     "NODO_CABA",       new ConexionLogistica(4500, 0.8));
        redLogistica.conectarNodos("NODO_AVELLANEDA", "NODO_CABA",       new ConexionLogistica(2000, 0.3));

        System.out.println("\nConexiones de transferencia interna:");
        System.out.println("  EZEIZA      <-> AVELLANEDA :  $3000  |  0.5 hs");
        System.out.println("  EZEIZA      <-> CABA       :  $4500  |  0.8 hs");
        System.out.println("  AVELLANEDA  <-> CABA       :  $2000  |  0.3 hs");
    }

    private static void registrarPedido(Pedido p) {
        gestorInfo.agregarPedido(p);
        gestorPedidos.encolarPedido(p);
        System.out.println("[CLIENTE] Carga pedido " + p.getIdPedido()
                + "  |  Tipo: " + p.getTipoEnvio()
                + "  |  Cantidad: " + p.getCapacidadRequerida()
                + "  |  Retiro en: " + p.getDestino());
    }

    // ==========================================================
    // Helpers de impresion
    // ==========================================================

    private static void printBanner(String text) {
        System.out.println("\n======================================================================");
        if (!text.isEmpty()) {
            System.out.println("    " + text);
            System.out.println("======================================================================");
        }
    }

    private static void printSeparator(String text) {
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("  " + text);
        System.out.println("----------------------------------------------------------------------");
    }

    private static void printEstadoDepositos(String titulo) {
        System.out.println("\n[" + titulo + "]");
        for (Deposito d : gestorDepositos.obtenerTodos()) {
            int utilizable = d.getCapacidadDisponible() - d.getCapacidadMinima();
            System.out.println("  - " + d.getIdDeposito() + " (" + d.getNombre() + "): "
                    + "disponible " + d.getCapacidadDisponible()
                    + " | utilizable " + utilizable
                    + " | minimo " + d.getCapacidadMinima());
        }
    }
}
