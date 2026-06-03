package com.loja.pedidos.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loja.pedidos.event.PedidoConcluidoEvent;
import com.loja.pedidos.exception.IntegrationException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PedidoEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String pedidoConcluidoTopic;

    public PedidoEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${loja.kafka.topics.pedido-concluido}") String pedidoConcluidoTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.pedidoConcluidoTopic = pedidoConcluidoTopic;
    }

    public void publicarPedidoConcluido(PedidoConcluidoEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(pedidoConcluidoTopic, event.pedidoId().toString(), payload)
                    .get(Duration.ofSeconds(5).toMillis(), TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException ex) {
            throw new IntegrationException("Nao foi possivel serializar o evento de pedido concluido");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IntegrationException("Publicacao do evento de pedido concluido foi interrompida");
        } catch (ExecutionException | TimeoutException ex) {
            throw new IntegrationException("Nao foi possivel publicar o evento de pedido concluido no Kafka");
        }
    }
}
