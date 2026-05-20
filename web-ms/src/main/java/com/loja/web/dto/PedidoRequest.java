package com.loja.web.dto;

import java.util.List;

public record PedidoRequest(
        Long clienteId,
        List<PedidoItemRequest> itens
) {
}
