package com.loja.pedidos.client;

import java.math.BigDecimal;

public record EstoqueClientResponse(
        Long id,
        String nomeProduto,
        BigDecimal valorUnitario,
        Integer quantidadeEstoque
) {
}
