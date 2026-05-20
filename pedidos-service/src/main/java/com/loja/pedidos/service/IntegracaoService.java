package com.loja.pedidos.service;

import com.loja.pedidos.client.ClienteClientResponse;
import com.loja.pedidos.client.EstoqueClientResponse;
import com.loja.pedidos.dto.DebitoEstoqueRequest;
import com.loja.pedidos.exception.BusinessException;
import com.loja.pedidos.exception.IntegrationException;
import com.loja.pedidos.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class IntegracaoService {

    private final RestClient restClient;
    private final String clientesServiceUrl;
    private final String estoqueServiceUrl;

    public IntegracaoService(
            RestClient restClient,
            @Value("${clientes.service.url}") String clientesServiceUrl,
            @Value("${estoque.service.url}") String estoqueServiceUrl
    ) {
        this.restClient = restClient;
        this.clientesServiceUrl = clientesServiceUrl;
        this.estoqueServiceUrl = estoqueServiceUrl;
    }

    public ClienteClientResponse buscarCliente(Long clienteId) {
        try {
            return restClient.get()
                    .uri(clientesServiceUrl + "/api/clientes/" + clienteId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new ResourceNotFoundException("Cliente nao encontrado: " + clienteId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new IntegrationException("Falha ao consultar o microservico de clientes");
                    })
                    .body(ClienteClientResponse.class);
        } catch (RestClientException ex) {
            throw new IntegrationException("Nao foi possivel acessar o microservico de clientes");
        }
    }

    public EstoqueClientResponse buscarProduto(Long produtoId) {
        try {
            return restClient.get()
                    .uri(estoqueServiceUrl + "/api/estoques/" + produtoId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new ResourceNotFoundException("Produto nao encontrado no estoque: " + produtoId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new IntegrationException("Falha ao consultar o microservico de estoque");
                    })
                    .body(EstoqueClientResponse.class);
        } catch (RestClientException ex) {
            throw new IntegrationException("Nao foi possivel acessar o microservico de estoque");
        }
    }

    public void debitarEstoque(DebitoEstoqueRequest request) {
        try {
            restClient.post()
                    .uri(estoqueServiceUrl + "/api/estoques/debito")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                        throw new BusinessException("Falha ao debitar estoque para concluir o pedido");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, response) -> {
                        throw new IntegrationException("Falha ao debitar estoque no microservico de estoque");
                    })
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new IntegrationException("Nao foi possivel debitar o estoque");
        }
    }
}
