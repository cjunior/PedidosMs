package com.loja.estoque.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loja.estoque.event.EstoqueDebitadoEvent;
import com.loja.estoque.event.EstoqueDebitoFalhouEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EstoqueEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String estoqueDebitadoTopic;
    private final String estoqueDebitoFalhouTopic;

    public EstoqueEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${loja.kafka.topics.estoque-debitado}") String estoqueDebitadoTopic,
            @Value("${loja.kafka.topics.estoque-debito-falhou}") String estoqueDebitoFalhouTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.estoqueDebitadoTopic = estoqueDebitadoTopic;
        this.estoqueDebitoFalhouTopic = estoqueDebitoFalhouTopic;
    }

    public void publicarEstoqueDebitado(Long pedidoId) {
        publicar(estoqueDebitadoTopic, pedidoId, new EstoqueDebitadoEvent(pedidoId, LocalDateTime.now()));
    }

    public void publicarFalhaEstoque(Long pedidoId, String motivo) {
        publicar(estoqueDebitoFalhouTopic, pedidoId, new EstoqueDebitoFalhouEvent(pedidoId, motivo, LocalDateTime.now()));
    }

    private void publicar(String topic, Long pedidoId, Object event) {
        try {
            kafkaTemplate.send(topic, pedidoId.toString(), objectMapper.writeValueAsString(event))
                    .get(Duration.ofSeconds(5).toMillis(), TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Nao foi possivel serializar evento de estoque", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicacao do evento de estoque foi interrompida", ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new IllegalStateException("Nao foi possivel publicar evento de estoque no Kafka", ex);
        }
    }
}
