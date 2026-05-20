package com.loja.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        String numero,
        Long clienteId,
        String status,
        LocalDateTime criadoEm,
        List<PedidoItemResponse> itens
) {
}
