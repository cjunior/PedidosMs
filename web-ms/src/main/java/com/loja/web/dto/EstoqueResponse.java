package com.loja.web.dto;

import java.math.BigDecimal;

public record EstoqueResponse(
        Long id,
        String nomeProduto,
        BigDecimal valorUnitario,
        Integer quantidadeEstoque
) {
}
