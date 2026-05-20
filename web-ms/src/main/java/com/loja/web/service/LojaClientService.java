package com.loja.web.service;

import com.loja.web.dto.ApiErrorResponse;
import com.loja.web.dto.ClienteForm;
import com.loja.web.dto.ClienteResponse;
import com.loja.web.dto.EstoqueForm;
import com.loja.web.dto.EstoqueResponse;
import com.loja.web.dto.PedidoForm;
import com.loja.web.dto.PedidoItemRequest;
import com.loja.web.dto.PedidoRequest;
import com.loja.web.dto.PedidoResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class LojaClientService {

    private final RestClient restClient;
    private final String clientesServiceUrl;
    private final String estoqueServiceUrl;
    private final String pedidosServiceUrl;

    public LojaClientService(
            RestClient restClient,
            @Value("${clientes.service.url}") String clientesServiceUrl,
            @Value("${estoque.service.url}") String estoqueServiceUrl,
            @Value("${pedidos.service.url}") String pedidosServiceUrl
    ) {
        this.restClient = restClient;
        this.clientesServiceUrl = clientesServiceUrl;
        this.estoqueServiceUrl = estoqueServiceUrl;
        this.pedidosServiceUrl = pedidosServiceUrl;
    }

    public List<ClienteResponse> listarClientes() {
        ClienteResponse[] response = read(() -> restClient.get()
                .uri(clientesServiceUrl + "/api/clientes")
                .retrieve()
                .body(ClienteResponse[].class));
        return response != null ? Arrays.asList(response) : Collections.emptyList();
    }

    public List<EstoqueResponse> listarProdutos() {
        EstoqueResponse[] response = read(() -> restClient.get()
                .uri(estoqueServiceUrl + "/api/estoques")
                .retrieve()
                .body(EstoqueResponse[].class));
        return response != null ? Arrays.asList(response) : Collections.emptyList();
    }

    public List<PedidoResponse> listarPedidos() {
        PedidoResponse[] response = read(() -> restClient.get()
                .uri(pedidosServiceUrl + "/api/pedidos")
                .retrieve()
                .body(PedidoResponse[].class));
        return response != null ? Arrays.asList(response) : Collections.emptyList();
    }

    public void criarCliente(ClienteForm form) {
        execute(() -> restClient.post()
                .uri(clientesServiceUrl + "/api/clientes")
                .body(form)
                .retrieve()
                .toBodilessEntity());
    }

    public void criarProduto(EstoqueForm form) {
        execute(() -> restClient.post()
                .uri(estoqueServiceUrl + "/api/estoques")
                .body(form)
                .retrieve()
                .toBodilessEntity());
    }

    public void criarPedido(PedidoForm form) {
        PedidoRequest request = new PedidoRequest(
                form.getClienteId(),
                form.getItens().stream()
                        .map(item -> new PedidoItemRequest(item.getProdutoId(), item.getQuantidade()))
                        .toList()
        );

        execute(() -> restClient.post()
                .uri(pedidosServiceUrl + "/api/pedidos")
                .body(request)
                .retrieve()
                .toBodilessEntity());
    }

    public void concluirPedido(Long pedidoId) {
        execute(() -> restClient.post()
                .uri(pedidosServiceUrl + "/api/pedidos/" + pedidoId + "/concluir")
                .retrieve()
                .toBodilessEntity());
    }

    private void execute(RestCall call) {
        try {
            call.run();
        } catch (RestClientResponseException ex) {
            throw new WebClientException(extractMessage(ex), ex.getStatusCode());
        } catch (Exception ex) {
            throw new WebClientException("Falha de comunicacao com os microservicos", null);
        }
    }

    private <T> T read(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RestClientResponseException ex) {
            throw new WebClientException(extractMessage(ex), ex.getStatusCode());
        } catch (Exception ex) {
            throw new WebClientException("Falha de comunicacao com os microservicos", null);
        }
    }

    private String extractMessage(RestClientResponseException ex) {
        try {
            ApiErrorResponse error = ex.getResponseBodyAs(ApiErrorResponse.class);
            if (error != null && error.details() != null && !error.details().isEmpty()) {
                return String.join(" | ", error.details());
            }
        } catch (Exception ignored) {
        }

        return ex.getStatusText() != null && !ex.getStatusText().isBlank()
                ? ex.getStatusText()
                : "Falha ao consumir endpoint remoto";
    }

    @FunctionalInterface
    private interface RestCall {
        void run();
    }

    public static class WebClientException extends RuntimeException {

        private final HttpStatusCode statusCode;

        public WebClientException(String message, HttpStatusCode statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public HttpStatusCode getStatusCode() {
            return statusCode;
        }
    }
}
