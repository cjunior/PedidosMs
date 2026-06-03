package com.loja.pedidos.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loja.pedidos.event.EstoqueDebitadoEvent;
import com.loja.pedidos.event.EstoqueDebitoFalhouEvent;
import com.loja.pedidos.service.PedidoService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EstoqueResultadoListener {

    private final ObjectMapper objectMapper;
    private final PedidoService pedidoService;

    public EstoqueResultadoListener(ObjectMapper objectMapper, PedidoService pedidoService) {
        this.objectMapper = objectMapper;
        this.pedidoService = pedidoService;
    }

    @KafkaListener(topics = "${loja.kafka.topics.estoque-debitado}", groupId = "${spring.kafka.consumer.group-id}")
    public void estoqueDebitado(String payload) throws JsonProcessingException {
        EstoqueDebitadoEvent event = objectMapper.readValue(payload, EstoqueDebitadoEvent.class);
        pedidoService.marcarEstoqueDebitado(event.pedidoId());
    }

    @KafkaListener(topics = "${loja.kafka.topics.estoque-debito-falhou}", groupId = "${spring.kafka.consumer.group-id}")
    public void estoqueDebitoFalhou(String payload) throws JsonProcessingException {
        EstoqueDebitoFalhouEvent event = objectMapper.readValue(payload, EstoqueDebitoFalhouEvent.class);
        pedidoService.marcarFalhaEstoque(event.pedidoId());
    }
}
