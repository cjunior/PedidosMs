package com.loja.pedidos.service;

import com.loja.pedidos.client.EstoqueClientResponse;
import com.loja.pedidos.dto.DebitoEstoqueItemRequest;
import com.loja.pedidos.dto.DebitoEstoqueRequest;
import com.loja.pedidos.dto.PedidoItemRequest;
import com.loja.pedidos.dto.PedidoItemResponse;
import com.loja.pedidos.dto.PedidoRequest;
import com.loja.pedidos.dto.PedidoResponse;
import com.loja.pedidos.entity.Pedido;
import com.loja.pedidos.entity.PedidoItem;
import com.loja.pedidos.entity.StatusPedido;
import com.loja.pedidos.exception.BusinessException;
import com.loja.pedidos.exception.ResourceNotFoundException;
import com.loja.pedidos.repository.PedidoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final IntegracaoService integracaoService;

    public PedidoService(PedidoRepository pedidoRepository, IntegracaoService integracaoService) {
        this.pedidoRepository = pedidoRepository;
        this.integracaoService = integracaoService;
    }

    public List<PedidoResponse> listar() {
        return pedidoRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PedidoResponse buscarPorId(Long id) {
        return toResponse(obterEntidade(id));
    }

    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        validarCliente(request.clienteId());

        Pedido pedido = new Pedido();
        pedido.setNumero(gerarNumeroPedido());
        pedido.setClienteId(request.clienteId());
        pedido.setStatus(StatusPedido.ABERTO);
        pedido.setCriadoEm(LocalDateTime.now());

        preencherItens(pedido, request.itens());
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse atualizar(Long id, PedidoRequest request) {
        Pedido pedido = obterEntidade(id);
        validarPedidoAberto(pedido);
        validarCliente(request.clienteId());

        pedido.setClienteId(request.clienteId());
        preencherItens(pedido, request.itens());
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public void excluir(Long id) {
        Pedido pedido = obterEntidade(id);
        validarPedidoAberto(pedido);
        pedidoRepository.delete(pedido);
    }

    @Transactional
    public PedidoResponse concluir(Long id) {
        Pedido pedido = obterEntidade(id);
        validarPedidoAberto(pedido);

        DebitoEstoqueRequest debitoRequest = new DebitoEstoqueRequest(
                pedido.getItens().stream()
                        .map(item -> new DebitoEstoqueItemRequest(item.getProdutoId(), item.getQuantidade()))
                        .toList()
        );

        integracaoService.debitarEstoque(debitoRequest);
        pedido.setStatus(StatusPedido.CONCLUIDO);

        return toResponse(pedidoRepository.save(pedido));
    }

    private Pedido obterEntidade(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado: " + id));
    }

    private void validarCliente(Long clienteId) {
        integracaoService.buscarCliente(clienteId);
    }

    private void validarPedidoAberto(Pedido pedido) {
        if (pedido.getStatus() == StatusPedido.CONCLUIDO) {
            throw new BusinessException("Pedidos concluidos nao podem ser alterados");
        }
    }

    private void preencherItens(Pedido pedido, List<PedidoItemRequest> itensRequest) {
        pedido.getItens().clear();

        for (PedidoItemRequest itemRequest : itensRequest) {
            EstoqueClientResponse produto = integracaoService.buscarProduto(itemRequest.produtoId());

            if (produto.quantidadeEstoque() < itemRequest.quantidade()) {
                throw new BusinessException("Estoque insuficiente para o produto " + produto.nomeProduto());
            }

            PedidoItem item = new PedidoItem();
            item.setPedido(pedido);
            item.setProdutoId(produto.id());
            item.setNomeProduto(produto.nomeProduto());
            item.setValorUnitario(produto.valorUnitario());
            item.setQuantidade(itemRequest.quantidade());
            pedido.getItens().add(item);
        }
    }

    private String gerarNumeroPedido() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private PedidoResponse toResponse(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getNumero(),
                pedido.getClienteId(),
                pedido.getStatus(),
                pedido.getCriadoEm(),
                pedido.getItens().stream()
                        .map(item -> new PedidoItemResponse(
                                item.getId(),
                                item.getProdutoId(),
                                item.getNomeProduto(),
                                item.getValorUnitario(),
                                item.getQuantidade()
                        ))
                        .toList()
        );
    }
}
