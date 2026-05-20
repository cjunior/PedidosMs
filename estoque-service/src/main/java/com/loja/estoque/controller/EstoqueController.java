package com.loja.estoque.controller;

import com.loja.estoque.dto.DebitoEstoqueRequest;
import com.loja.estoque.dto.EstoqueRequest;
import com.loja.estoque.dto.EstoqueResponse;
import com.loja.estoque.service.EstoqueService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estoques")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @GetMapping
    public List<EstoqueResponse> listar() {
        return estoqueService.listar();
    }

    @GetMapping("/{id}")
    public EstoqueResponse buscarPorId(@PathVariable Long id) {
        return estoqueService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EstoqueResponse criar(@Valid @RequestBody EstoqueRequest request) {
        return estoqueService.criar(request);
    }

    @PutMapping("/{id}")
    public EstoqueResponse atualizar(@PathVariable Long id, @Valid @RequestBody EstoqueRequest request) {
        return estoqueService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        estoqueService.excluir(id);
    }

    @PostMapping("/debito")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void debitar(@Valid @RequestBody DebitoEstoqueRequest request) {
        estoqueService.debitar(request);
    }
}
