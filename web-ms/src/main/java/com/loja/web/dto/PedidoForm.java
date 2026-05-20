package com.loja.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class PedidoForm {

    @NotNull(message = "Cliente e obrigatorio")
    private Long clienteId;

    @Valid
    @NotEmpty(message = "O pedido deve conter ao menos um item")
    private List<PedidoItemForm> itens = new ArrayList<>(List.of(new PedidoItemForm()));

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<PedidoItemForm> getItens() {
        return itens;
    }

    public void setItens(List<PedidoItemForm> itens) {
        this.itens = itens;
    }
}
