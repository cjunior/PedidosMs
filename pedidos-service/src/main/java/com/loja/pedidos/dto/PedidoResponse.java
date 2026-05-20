package com.loja.pedidos.dto;

import com.loja.pedidos.entity.StatusPedido;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        String numero,
        Long clienteId,
        StatusPedido status,
        LocalDateTime criadoEm,
        List<PedidoItemResponse> itens
) {
}
