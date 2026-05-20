package com.loja.clientes.dto;

public record ClienteResponse(
        Long id,
        String nomeCompleto,
        String cpf,
        String endereco,
        String telefone
) {
}
