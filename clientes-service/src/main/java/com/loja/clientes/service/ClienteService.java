package com.loja.clientes.service;

import com.loja.clientes.dto.ClienteRequest;
import com.loja.clientes.dto.ClienteResponse;
import com.loja.clientes.entity.Cliente;
import com.loja.clientes.exception.BusinessException;
import com.loja.clientes.exception.ResourceNotFoundException;
import com.loja.clientes.repository.ClienteRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ClienteResponse buscarPorId(Long id) {
        return toResponse(obterEntidade(id));
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        if (clienteRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("Ja existe cliente com o CPF informado");
        }

        Cliente cliente = new Cliente();
        preencher(cliente, request);
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = obterEntidade(id);

        clienteRepository.findByCpf(request.cpf())
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new BusinessException("Ja existe cliente com o CPF informado");
                });

        preencher(cliente, request);
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = obterEntidade(id);
        clienteRepository.delete(cliente);
    }

    private Cliente obterEntidade(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado: " + id));
    }

    private void preencher(Cliente cliente, ClienteRequest request) {
        cliente.setNomeCompleto(request.nomeCompleto());
        cliente.setCpf(request.cpf());
        cliente.setEndereco(request.endereco());
        cliente.setTelefone(request.telefone());
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNomeCompleto(),
                cliente.getCpf(),
                cliente.getEndereco(),
                cliente.getTelefone()
        );
    }
}
