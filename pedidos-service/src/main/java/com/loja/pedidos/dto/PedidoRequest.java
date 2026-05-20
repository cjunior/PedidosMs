package com.loja.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PedidoRequest(
        @NotNull(message = "Cliente e obrigatorio")
        Long clienteId,
        @Valid
        @NotEmpty(message = "O pedido deve conter ao menos um item")
        List<PedidoItemRequest> itens
) {
}
