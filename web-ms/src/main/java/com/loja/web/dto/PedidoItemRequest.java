package com.loja.web.dto;

public record PedidoItemRequest(
        Long produtoId,
        Integer quantidade
) {
}
