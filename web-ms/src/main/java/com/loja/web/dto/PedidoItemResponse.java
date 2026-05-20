package com.loja.web.dto;

import java.math.BigDecimal;

public record PedidoItemResponse(
        Long id,
        Long produtoId,
        String nomeProduto,
        BigDecimal valorUnitario,
        Integer quantidade
) {
}
