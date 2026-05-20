package com.loja.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record EstoqueRequest(
        @NotBlank(message = "Nome do produto e obrigatorio")
        String nomeProduto,
        @NotNull(message = "Valor unitario e obrigatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "Valor unitario deve ser maior que zero")
        BigDecimal valorUnitario,
        @NotNull(message = "Quantidade em estoque e obrigatoria")
        @Min(value = 0, message = "Quantidade em estoque nao pode ser negativa")
        Integer quantidadeEstoque
) {
}
