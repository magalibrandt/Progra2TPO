package ecommerce.controller;

import ecommerce.model.*;
import ecommerce.tda.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MotorAsignacionLogistica {
    private GestorPedidosTDA gestorPedidos;
    private GestorInformacionPedidoTDA gestorInfo;
    private GestorDepositosTDA gestorDepositos;
    private ArbolCapacidadDepositosTDA arbolCapacidades;
    private RedLogisticaTDA redLogistica;

    public MotorAsignacionLogistica(GestorPedidosTDA gp, GestorInformacionPedidoTDA gi, GestorDepositosTDA gd, ArbolCapacidadDepositosTDA ac, RedLogisticaTDA rl) {
        this.gestorPedidos = gp;
        this.gestorInfo = gi;
        this.gestorDepositos = gd;
        this.arbolCapacidades = ac;
        this.redLogistica = rl;
    }

    public List<AsignacionDeposito> procesarSiguientePedido() {
        if (gestorPedidos.estaVacio()) {
            System.out.println("[MOTOR LOGÍSTICO] La cola de prioridad se encuentra vacía. No hay demandas pendientes.");
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
        if (pedido == null) throw new IllegalArgumentException("El pedido no existe en el registro.");

        System.out.println("[DICCIONARIO PEDIDOS] Datos validados: Requiere " + pedido.getCapacidadRequerida() + " unidades con destino a: " + pedido.getDestino());

        // 1. Consultar árbol AVL dinámico de capacidades disponibles
        System.out.println("[ÁRBOL AVL CAPACIDADES] Interrogando nodos jerárquicos para filtrar depósitos con espacio disponible...");
        List<Deposito> candidatos = arbolCapacidades.obtenerDepositosConCapacidadDisponible();
        
        System.out.print(" -> Depósitos detectados en el AVL con espacio > 0: [ ");
        for(Deposito c : candidatos) {
            System.out.print(c.getIdDeposito() + "(" + c.getCapacidadDisponible() + "u) ");
        }
        System.out.println("]");

        // Filtrar localmente en el motor por las dudas de que quede algún residuo con 0 unidades
        List<Deposito> candidatosValidos = new ArrayList<>();
        for (Deposito d : candidatos) {
            if (d.getCapacidadDisponible() > 0) {
                candidatosValidos.add(d);
            }
        }

        if (candidatosValidos.isEmpty()) {
            throw new IllegalStateException("Falla crítica: No hay capacidad suficiente en la red logística para cubrir la orden.");
        }

        // 2. Evaluar y ordenar candidatos según las métricas del Grafo multicriterio
        System.out.println("[GRAFO RED LOGÍSTICA] Evaluando rutas e integrando pesos según criterio comercial (" + pedido.getTipoEnvio() + ")...");
        for (Deposito d : candidatosValidos) {
            double costoRuta = redLogistica.obtenerCostoRuta(d.getIdNodoLogistico(), pedido.getDestino(), pedido.getTipoEnvio());
            System.out.println("   * Evaluando " + d.getIdDeposito() + " (" + d.getNombre() + ") -> Peso de ruta calculado: " + costoRuta);
        }

        candidatosValidos.sort(Comparator.comparingDouble(
            d -> redLogistica.obtenerCostoRuta(d.getIdNodoLogistico(), pedido.getDestino(), pedido.getTipoEnvio())
        ));

        // 3. Ejecutar algoritmo de asignación fraccionada/distribuida
        List<AsignacionDeposito> asignaciones = new ArrayList<>();
        int restante = pedido.getCapacidadRequerida();

        System.out.println("\n[MOTOR ASIGNACIÓN] Iniciando algoritmo de cobertura distribuida...");
        for (Deposito deposito : candidatosValidos) {
            if (restante == 0) break;

            int capAnterior = deposito.getCapacidadDisponible();
            int cantidadAsignada = Math.min(restante, capAnterior);
            double costo = redLogistica.obtenerCostoRuta(deposito.getIdNodoLogistico(), pedido.getDestino(), pedido.getTipoEnvio());

            System.out.println(" -> Removiendo " + cantidadAsignada + " unidades de " + deposito.getIdDeposito() + ". (Stock anterior: " + capAnterior + ")");
            gestorDepositos.reservarCapacidad(deposito.getIdDeposito(), cantidadAsignada);
            
            // --- OPTIMIZACIÓN CLAVE ---
            // Si el depósito se queda sin espacio (capacidad nueva = 0), lo borramos directamente del AVL
            if (deposito.getCapacidadDisponible() == 0) {
                arbolCapacidades.eliminarDeposito(deposito);
                System.out.println("   [AVL UPDATE] Depósito " + deposito.getIdDeposito() + " se quedó sin espacio. Removido por completo del AVL.");
            } else {
                arbolCapacidades.actualizarCapacidad(deposito, capAnterior);
                System.out.println("   [AVL UPDATE] Depósito " + deposito.getIdDeposito() + " movido al nodo de capacidad: " + deposito.getCapacidadDisponible());
            }

            asignaciones.add(new AsignacionDeposito(deposito.getIdDeposito(), cantidadAsignada, costo));
            restante -= cantidadAsignada;
            System.out.println("   * Capacidad del pedido pendiente de cubrir: " + restante + " unidades.");
        }

        if (restante > 0) throw new IllegalStateException("Falla crítica: No hay capacidad suficiente en la red logística para cubrir la orden.");

        pedido.setEstado(EstadoPedido.ASIGNADO);
        gestorInfo.actualizarPedido(pedido);
        System.out.println("[ESTADO PEDIDO] " + pedido.getIdPedido() + " mutó exitosamente a: " + pedido.getEstado());
        System.out.println("======================================================================");

        return asignaciones;
    }
}