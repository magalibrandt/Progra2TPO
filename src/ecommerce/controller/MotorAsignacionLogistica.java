package ecommerce.controller;

import ecommerce.model.*;
import ecommerce.tda.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MotorAsignacionLogistica {
    private final GestorPedidosTDA gestorPedidos;
    private final GestorInformacionPedidoTDA gestorInfo;
    private final GestorDepositosTDA gestorDepositos;
    private final ArbolCapacidadDepositosTDA arbolCapacidad;
    private final RedLogisticaTDA redLogistica;

    public MotorAsignacionLogistica(GestorPedidosTDA gp, GestorInformacionPedidoTDA gi, GestorDepositosTDA gd, ArbolCapacidadDepositosTDA ac, RedLogisticaTDA rl) {
        this.gestorPedidos = gp;
        this.gestorInfo = gi;
        this.gestorDepositos = gd;
        this.arbolCapacidad = ac;
        this.redLogistica = rl;
    }

    public List<AsignacionDeposito> procesarSiguientePedido() {
        if (gestorPedidos.estaVacio()) {
            System.out.println("[MOTOR LOGÍSTICO] La cola de prioridad se encuentra vacía.");
            return new ArrayList<>();
        }
        Pedido pedido = gestorPedidos.desencolarPedido();
        System.out.println("\n======================================================================");
        System.out.println("[COLA PRIORIDAD] Extrayendo siguiente orden para procesamiento...");
        System.out.println(" -> Pedido ID: " + pedido.getIdPedido() + " | Prioridad Comercial: " + pedido.getTipoEnvio());
        return asignarPedido(pedido.getIdPedido());
    }

    public List<AsignacionDeposito> asignarPedido(String idPedido) {
        Pedido pedido = gestorInfo.obtenerPedido(idPedido);
        if (pedido == null) throw new IllegalArgumentException("El pedido no existe en el índice.");

        System.out.println("[ÍNDICE ÁRBOL B+] Datos validados de forma logarítmica: Requiere " + pedido.getCapacidadRequerida() + " unidades.");

        List<Deposito> candidatos = arbolCapacidad.obtenerDepositosConCapacidadDisponible();
        List<Deposito> candidatosValidos = new ArrayList<>();
        for (Deposito d : candidatos) {
            if (d.getCapacidadDisponible() > 0) candidatosValidos.add(d);
        }

        if (candidatosValidos.isEmpty()) throw new IllegalStateException("Falla de cobertura volumétrica global.");

        candidatosValidos.sort(Comparator.comparingDouble(
            d -> redLogistica.obtenerCostoRuta(d.getIdNodoLogistico(), pedido.getDestino(), pedido.getTipoEnvio())
        ));

        List<AsignacionDeposito> asignaciones = new ArrayList<>();
        int restante = pedido.getCapacidadRequerida();

        System.out.println("\n[MOTOR ASIGNACIÓN] Iniciando algoritmo de cobertura distribuida...");
        for (Deposito deposito : candidatosValidos) {
            if (restante == 0) break;

            int capAnterior = deposito.getCapacidadDisponible();
            int cantidadAsignada = Math.min(restante, capAnterior);
            double costo = redLogistica.obtenerCostoRuta(deposito.getIdNodoLogistico(), pedido.getDestino(), pedido.getTipoEnvio());

            // 1. Primero informamos la acción de asignación lógica
            System.out.println("   -> Asignando desde " + deposito.getIdDeposito() + ": extraídas " + cantidadAsignada + " unidades.");
            gestorDepositos.reservarCapacidad(deposito.getIdDeposito(), cantidadAsignada);
            
            // 2. Luego informamos el impacto consecuencia en el AVL
            if (deposito.getCapacidadDisponible() == 0) {
                arbolCapacidad.eliminarDeposito(deposito);
                System.out.println("     [AVL UPDATE] Depósito " + deposito.getIdDeposito() + " removido por stock en 0.");
            } else {
                arbolCapacidad.actualizarCapacidad(deposito, capAnterior);
                System.out.println("     [AVL UPDATE] Depósito " + deposito.getIdDeposito() + " movido al nodo: " + deposito.getCapacidadDisponible());
            }

            asignaciones.add(new AsignacionDeposito(deposito.getIdDeposito(), cantidadAsignada, costo));
            restante -= cantidadAsignada;
        }

        if (restante > 0) throw new IllegalStateException("No hay capacidad suficiente en la red.");

        // 3. Mostramos el resumen consolidado dentro del bloque operacional antes del cierre
        System.out.println("\n[RESUMEN DE DESPACHO CONSOLIDADO]");
        for (AsignacionDeposito asig : asignaciones) {
            System.out.println("  * Asignacion -> Deposito: " + asig.getIdDeposito() + " | Cantidad: " + asig.getCantidad() + " | Peso Logistico: " + asig.getCostoRuta());
        }

        pedido.setEstado(EstadoPedido.ASIGNADO);
        gestorInfo.actualizarPedido(pedido); 
        System.out.println("\n[ESTADO PEDIDO] Guardado en la hoja del Árbol B+: ASIGNADO");
        System.out.println("======================================================================");

        return asignaciones;
    }
}