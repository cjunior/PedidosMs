package com.loja.clientes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank(message = "Nome completo e obrigatorio")
        String nomeCompleto,
        @NotBlank(message = "CPF e obrigatorio")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 digitos numericos")
        String cpf,
        @NotBlank(message = "Endereco e obrigatorio")
        String endereco,
        @NotBlank(message = "Telefone e obrigatorio")
        @Size(max = 20, message = "Telefone deve ter no maximo 20 caracteres")
        String telefone
) {
}
