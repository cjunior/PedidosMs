package com.loja.estoque.event;

import java.time.LocalDateTime;

public record EstoqueDebitoFalhouEvent(
        Long pedidoId,
        String motivo,
        LocalDateTime ocorridoEm
) {
}
