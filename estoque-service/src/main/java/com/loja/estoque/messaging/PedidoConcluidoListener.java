package com.loja.estoque.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loja.estoque.dto.DebitoEstoqueItemRequest;
import com.loja.estoque.dto.DebitoEstoqueRequest;
import com.loja.estoque.event.PedidoConcluidoEvent;
import com.loja.estoque.service.EstoqueService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoConcluidoListener {

    private final ObjectMapper objectMapper;
    private final EstoqueService estoqueService;
    private final EstoqueEventPublisher estoqueEventPublisher;

    public PedidoConcluidoListener(
            ObjectMapper objectMapper,
            EstoqueService estoqueService,
            EstoqueEventPublisher estoqueEventPublisher
    ) {
        this.objectMapper = objectMapper;
        this.estoqueService = estoqueService;
        this.estoqueEventPublisher = estoqueEventPublisher;
    }

    @KafkaListener(topics = "${loja.kafka.topics.pedido-concluido}", groupId = "${spring.kafka.consumer.group-id}")
    public void pedidoConcluido(String payload) throws JsonProcessingException {
        PedidoConcluidoEvent event = objectMapper.readValue(payload, PedidoConcluidoEvent.class);
        try {
            DebitoEstoqueRequest request = new DebitoEstoqueRequest(
                    event.itens().stream()
                            .map(item -> new DebitoEstoqueItemRequest(item.produtoId(), item.quantidade()))
                            .toList()
            );
            estoqueService.debitarPedido(event.pedidoId(), request);
            estoqueEventPublisher.publicarEstoqueDebitado(event.pedidoId());
        } catch (RuntimeException ex) {
            estoqueEventPublisher.publicarFalhaEstoque(event.pedidoId(), ex.getMessage());
        }
    }
}
