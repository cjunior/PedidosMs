package com.loja.estoque.event;

public record PedidoConcluidoItemEvent(
        Long produtoId,
        Integer quantidade
) {
}
