package com.loja.estoque.service;

import com.loja.estoque.dto.DebitoEstoqueRequest;
import com.loja.estoque.dto.EstoqueRequest;
import com.loja.estoque.dto.EstoqueResponse;
import com.loja.estoque.entity.Estoque;
import com.loja.estoque.exception.BusinessException;
import com.loja.estoque.exception.ResourceNotFoundException;
import com.loja.estoque.repository.EstoqueRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;

    public EstoqueService(EstoqueRepository estoqueRepository) {
        this.estoqueRepository = estoqueRepository;
    }

    public List<EstoqueResponse> listar() {
        return estoqueRepository.findAll().stream().map(this::toResponse).toList();
    }

    public EstoqueResponse buscarPorId(Long id) {
        return toResponse(obterEntidade(id));
    }

    @Transactional
    public EstoqueResponse criar(EstoqueRequest request) {
        Estoque estoque = new Estoque();
        preencher(estoque, request);
        return toResponse(estoqueRepository.save(estoque));
    }

    @Transactional
    public EstoqueResponse atualizar(Long id, EstoqueRequest request) {
        Estoque estoque = obterEntidade(id);
        preencher(estoque, request);
        return toResponse(estoqueRepository.save(estoque));
    }

    @Transactional
    public void excluir(Long id) {
        Estoque estoque = obterEntidade(id);
        estoqueRepository.delete(estoque);
    }

    @Transactional
    public void debitar(DebitoEstoqueRequest request) {
        Map<Long, Integer> quantidades = new HashMap<>();
        request.itens().forEach(item -> quantidades.merge(item.produtoId(), item.quantidade(), Integer::sum));

        List<Estoque> produtos = estoqueRepository.findAllById(quantidades.keySet());
        if (produtos.size() != quantidades.size()) {
            throw new ResourceNotFoundException("Um ou mais produtos informados nao existem no estoque");
        }

        for (Estoque produto : produtos) {
            Integer quantidadeDebito = quantidades.get(produto.getId());
            if (produto.getQuantidadeEstoque() < quantidadeDebito) {
                throw new BusinessException("Estoque insuficiente para o produto " + produto.getNomeProduto());
            }
        }

        for (Estoque produto : produtos) {
            Integer quantidadeDebito = quantidades.get(produto.getId());
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - quantidadeDebito);
        }

        estoqueRepository.saveAll(produtos);
    }

    private Estoque obterEntidade(Long id) {
        return estoqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado no estoque: " + id));
    }

    private void preencher(Estoque estoque, EstoqueRequest request) {
        estoque.setNomeProduto(request.nomeProduto());
        estoque.setValorUnitario(request.valorUnitario());
        estoque.setQuantidadeEstoque(request.quantidadeEstoque());
    }

    private EstoqueResponse toResponse(Estoque estoque) {
        return new EstoqueResponse(
                estoque.getId(),
                estoque.getNomeProduto(),
                estoque.getValorUnitario(),
                estoque.getQuantidadeEstoque()
        );
    }
}
