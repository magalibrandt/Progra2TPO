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

    public MotorAsignacionLogistica(GestorPedidosTDA gp, GestorInformacionPedidoTDA gi,
                                    GestorDepositosTDA gd, ArbolCapacidadDepositosTDA ac,
                                    RedLogisticaTDA rl) {
        this.gestorPedidos = gp;
        this.gestorInfo = gi;
        this.gestorDepositos = gd;
        this.arbolCapacidad = ac;
        this.redLogistica = rl;
    }

    public List<AsignacionDeposito> procesarSiguientePedido() {
        // Salta pedidos cancelados que hayan quedado en la cola.
        while (!gestorPedidos.estaVacio()) {
            Pedido pedido = gestorPedidos.desencolarPedido();
            Pedido datos = gestorInfo.obtenerPedido(pedido.getIdPedido());
            if (datos != null && datos.getEstado() == EstadoPedido.CANCELADO) {
                System.out.println("[COLA PRIORIDAD] Pedido " + pedido.getIdPedido()
                    + " fue cancelado mientras estaba en la cola. Salteando.");
                continue;
            }
            System.out.println("\n======================================================================");
            System.out.println("[COLA PRIORIDAD] Extrayendo siguiente orden para procesamiento...");
            System.out.println(" -> Pedido ID: " + pedido.getIdPedido()
                + " | Prioridad Comercial: " + pedido.getTipoEnvio());
            return asignarPedido(pedido.getIdPedido());
        }
        System.out.println("[MOTOR LOGISTICO] La cola de prioridad se encuentra vacia.");
        return new ArrayList<>();
    }

    public List<AsignacionDeposito> asignarPedido(String idPedido) {
        Pedido pedido = gestorInfo.obtenerPedido(idPedido);
        if (pedido == null) throw new IllegalArgumentException("El pedido no existe en el indice.");

        System.out.println("[INDICE ARBOL B+] Requiere " + pedido.getCapacidadRequerida()
            + " unidades. Deposito de retiro: " + pedido.getDestino());

        // 1. Obtener candidatos con capacidad utilizable (por encima del stock minimo).
        List<Deposito> candidatos = arbolCapacidad.obtenerDepositosConCapacidadDisponible();
        List<Deposito> candidatosValidos = new ArrayList<>();
        for (Deposito d : candidatos) {
            if ((d.getCapacidadDisponible() - d.getCapacidadMinima()) > 0) {
                candidatosValidos.add(d);
            }
        }
        if (candidatosValidos.isEmpty()) {
            throw new IllegalStateException("No hay capacidad utilizable en la red para cubrir la orden.");
        }

        // 2. Ordenar candidatos segun el tipo de envio.
        String idDestino = pedido.getDestino();
        if (pedido.getTipoEnvio() == TipoEnvio.NORMAL) {
            candidatosValidos.sort(Comparator.comparingDouble(
                d -> redLogistica.obtenerCostoRuta(d.getIdNodoLogistico(), idDestino)
            ));
        } else {
            candidatosValidos.sort(Comparator.comparingDouble(
                d -> redLogistica.obtenerTiempoRuta(d.getIdNodoLogistico(), idDestino)
            ));
        }

        // 3. Algoritmo de cobertura distribuida.
        List<AsignacionDeposito> asignaciones = new ArrayList<>();
        int restante = pedido.getCapacidadRequerida();

        System.out.println("\n[MOTOR ASIGNACION] Iniciando algoritmo de cobertura distribuida...");
        for (Deposito deposito : candidatosValidos) {
            if (restante == 0) break;

            int capAnterior = deposito.getCapacidadDisponible();
            int utilizable = capAnterior - deposito.getCapacidadMinima();
            int cantidadAsignada = Math.min(restante, utilizable);
            if (cantidadAsignada <= 0) continue;

            double valorRuta = (pedido.getTipoEnvio() == TipoEnvio.NORMAL)
                ? redLogistica.obtenerCostoRuta(deposito.getIdNodoLogistico(), idDestino)
                : redLogistica.obtenerTiempoRuta(deposito.getIdNodoLogistico(), idDestino);

            System.out.println("   -> Asignando desde " + deposito.getIdDeposito()
                + ": " + cantidadAsignada + " unidades.");
            gestorDepositos.reservarCapacidad(deposito.getIdDeposito(), cantidadAsignada);

            // Actualizar AVL tras la reserva.
            int nuevoUtilizable = deposito.getCapacidadDisponible() - deposito.getCapacidadMinima();
            if (nuevoUtilizable <= 0) {
                arbolCapacidad.eliminarDeposito(deposito);
                System.out.println("     [AVL UPDATE] " + deposito.getIdDeposito()
                    + " removido (capacidad utilizable agotada).");
            } else {
                arbolCapacidad.actualizarCapacidad(deposito, capAnterior);
                System.out.println("     [AVL UPDATE] " + deposito.getIdDeposito()
                    + " movido al nodo de capacidad: " + deposito.getCapacidadDisponible());
            }

            asignaciones.add(new AsignacionDeposito(deposito.getIdDeposito(), cantidadAsignada, valorRuta));
            restante -= cantidadAsignada;
        }

        if (restante > 0) {
            throw new IllegalStateException("No hay capacidad suficiente en la red para cubrir la orden.");
        }

        // 4. Persistir las asignaciones en el pedido (necesario para cancelacion).
        pedido.setAsignaciones(asignaciones);

        // 5. Determinar el modo de entrega: retiro en el dia vs consolidacion.
// Hay retiro en el dia si toda la cobertura sale de un solo deposito y ese deposito
// corresponde al nodo logistico de retiro elegido por el cliente.
        boolean retiroEnElDia = false;
        if (asignaciones.size() == 1) {
            Deposito unicoDep = gestorDepositos.obtenerDeposito(asignaciones.get(0).getIdDeposito());
            if (unicoDep != null && unicoDep.getIdNodoLogistico().equals(pedido.getDestino())) {
                retiroEnElDia = true;
            }
        }

        System.out.println("\n[RESUMEN DE DESPACHO]");
        for (AsignacionDeposito a : asignaciones) {
            System.out.println("  * " + a.getIdDeposito() + " aporta " + a.getCantidad()
                + " unidades | criterio: " + a.getCostoRuta());
        }

        if (retiroEnElDia) {
            System.out.println("[RETIRO INMEDIATO] El pedido " + pedido.getIdPedido()
                + " queda disponible para retiro en el dia en " + pedido.getDestino() + ".");
        } else {
            System.out.println("[CONSOLIDACION] El pedido " + pedido.getIdPedido()
                + " requiere transferencia desde " + asignaciones.size()
                + " deposito(s) hacia el deposito de retiro " + pedido.getDestino() + ".");
        }

        // 6. Marcar el pedido como ASIGNADO y persistir.
        pedido.setEstado(EstadoPedido.ASIGNADO);
        gestorInfo.actualizarPedido(pedido);
        System.out.println("[ESTADO PEDIDO] ASIGNADO (persistido en el Arbol B+).");
        System.out.println("======================================================================");

        return asignaciones;
    }

    // Cancelacion de un pedido: libera capacidades reservadas y actualiza el AVL.
    public void cancelarPedido(String idPedido) {
        Pedido pedido = gestorInfo.obtenerPedido(idPedido);
        if (pedido == null) throw new IllegalArgumentException("El pedido no existe en el indice.");
        if (pedido.getEstado() == EstadoPedido.CANCELADO) return;

        // Solo si estaba ASIGNADO hay que revertir reservas.
        if (pedido.getEstado() == EstadoPedido.ASIGNADO) {
            for (AsignacionDeposito a : pedido.getAsignaciones()) {
                Deposito d = gestorDepositos.obtenerDeposito(a.getIdDeposito());
                if (d == null) continue;

                int capAnterior = d.getCapacidadDisponible();
                int utilAnterior = capAnterior - d.getCapacidadMinima();

                gestorDepositos.liberarCapacidad(d.getIdDeposito(), a.getCantidad());

                // Si el deposito estaba fuera del AVL por capacidad agotada, reinsertarlo.
                // Si seguia dentro, reubicarlo en el nodo de la nueva capacidad.
                int utilNuevo = d.getCapacidadDisponible() - d.getCapacidadMinima();
                if (utilAnterior <= 0 && utilNuevo > 0) {
                    arbolCapacidad.insertarDeposito(d);
                } else {
                    arbolCapacidad.actualizarCapacidad(d, capAnterior);
                }
            }
            pedido.getAsignaciones().clear();
        }

        // Marcar como CANCELADO en el Arbol B+.
        gestorInfo.cancelarPedido(idPedido);
        System.out.println("[CANCELACION] Pedido " + idPedido + " cancelado y capacidades reintegradas.");
    }
}
