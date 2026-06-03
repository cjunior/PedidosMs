package com.loja.pedidos.event;

import java.time.LocalDateTime;

public record EstoqueDebitadoEvent(
        Long pedidoId,
        LocalDateTime ocorridoEm
) {
}
