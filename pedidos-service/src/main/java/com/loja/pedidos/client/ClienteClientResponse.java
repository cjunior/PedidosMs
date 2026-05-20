package com.loja.pedidos.client;

public record ClienteClientResponse(
        Long id,
        String nomeCompleto,
        String cpf,
        String endereco,
        String telefone
) {
}
