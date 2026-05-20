package com.loja.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class EstoqueForm {

    @NotBlank(message = "Nome do produto e obrigatorio")
    private String nomeProduto;

    @NotNull(message = "Valor unitario e obrigatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor unitario deve ser maior que zero")
    private BigDecimal valorUnitario;

    @NotNull(message = "Quantidade em estoque e obrigatoria")
    @Min(value = 0, message = "Quantidade em estoque nao pode ser negativa")
    private Integer quantidadeEstoque;

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(Integer quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }
}
