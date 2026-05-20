package com.loja.web.dto;

public record ClienteResponse(
        Long id,
        String nomeCompleto,
        String cpf,
        String endereco,
        String telefone
) {
}
