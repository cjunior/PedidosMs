package com.loja.estoque.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record DebitoEstoqueRequest(
        @Valid
        @NotEmpty(message = "A lista de itens para debito e obrigatoria")
        List<DebitoEstoqueItemRequest> itens
) {
}
