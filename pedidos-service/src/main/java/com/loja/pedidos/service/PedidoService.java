package com.loja.pedidos.service;

import com.loja.pedidos.client.EstoqueClientResponse;
import com.loja.pedidos.dto.PedidoItemRequest;
import com.loja.pedidos.dto.PedidoItemResponse;
import com.loja.pedidos.dto.PedidoRequest;
import com.loja.pedidos.dto.PedidoResponse;
import com.loja.pedidos.entity.Pedido;
import com.loja.pedidos.entity.PedidoItem;
import com.loja.pedidos.entity.StatusPedido;
import com.loja.pedidos.event.PedidoConcluidoEvent;
import com.loja.pedidos.event.PedidoConcluidoItemEvent;
import com.loja.pedidos.exception.BusinessException;
import com.loja.pedidos.exception.ResourceNotFoundException;
import com.loja.pedidos.messaging.PedidoEventPublisher;
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
    private final PedidoEventPublisher pedidoEventPublisher;

    public PedidoService(
            PedidoRepository pedidoRepository,
            IntegracaoService integracaoService,
            PedidoEventPublisher pedidoEventPublisher
    ) {
        this.pedidoRepository = pedidoRepository;
        this.integracaoService = integracaoService;
        this.pedidoEventPublisher = pedidoEventPublisher;
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
        validarPedidoPodeSerConcluido(pedido);

        pedido.setStatus(StatusPedido.PROCESSANDO_ESTOQUE);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        PedidoConcluidoEvent event = new PedidoConcluidoEvent(
                pedidoSalvo.getId(),
                pedidoSalvo.getNumero(),
                pedidoSalvo.getClienteId(),
                LocalDateTime.now(),
                pedidoSalvo.getItens().stream()
                        .map(item -> new PedidoConcluidoItemEvent(item.getProdutoId(), item.getQuantidade()))
                        .toList()
        );
        pedidoEventPublisher.publicarPedidoConcluido(event);

        return toResponse(pedidoSalvo);
    }

    @Transactional
    public void marcarEstoqueDebitado(Long pedidoId) {
        Pedido pedido = obterEntidade(pedidoId);
        if (pedido.getStatus() == StatusPedido.PROCESSANDO_ESTOQUE) {
            pedido.setStatus(StatusPedido.CONCLUIDO);
            pedidoRepository.save(pedido);
        }
    }

    @Transactional
    public void marcarFalhaEstoque(Long pedidoId) {
        Pedido pedido = obterEntidade(pedidoId);
        if (pedido.getStatus() == StatusPedido.PROCESSANDO_ESTOQUE) {
            pedido.setStatus(StatusPedido.FALHA_ESTOQUE);
            pedidoRepository.save(pedido);
        }
    }

    private Pedido obterEntidade(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado: " + id));
    }

    private void validarCliente(Long clienteId) {
        integracaoService.buscarCliente(clienteId);
    }

    private void validarPedidoAberto(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.ABERTO) {
            throw new BusinessException("Apenas pedidos abertos podem ser alterados");
        }
    }

    private void validarPedidoPodeSerConcluido(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.ABERTO && pedido.getStatus() != StatusPedido.FALHA_ESTOQUE) {
            throw new BusinessException("Pedido nao pode ser concluido no status atual");
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
