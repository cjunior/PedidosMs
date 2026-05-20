package com.loja.pedidos.dto;

public record DebitoEstoqueItemRequest(
        Long produtoId,
        Integer quantidade
) {
}
