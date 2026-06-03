package com.loja.pedidos.event;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoConcluidoEvent(
        Long pedidoId,
        String numero,
        Long clienteId,
        LocalDateTime ocorridoEm,
        List<PedidoConcluidoItemEvent> itens
) {
}
