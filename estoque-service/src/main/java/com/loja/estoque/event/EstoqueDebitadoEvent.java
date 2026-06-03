package com.loja.estoque.event;

import java.time.LocalDateTime;

public record EstoqueDebitadoEvent(
        Long pedidoId,
        LocalDateTime ocorridoEm
) {
}
