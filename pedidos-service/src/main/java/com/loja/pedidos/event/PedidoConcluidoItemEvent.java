package com.loja.pedidos.event;

public record PedidoConcluidoItemEvent(
        Long produtoId,
        Integer quantidade
) {
}
