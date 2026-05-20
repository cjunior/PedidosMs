package com.loja.pedidos.dto;

import java.util.List;

public record DebitoEstoqueRequest(
        List<DebitoEstoqueItemRequest> itens
) {
}
