package com.loja.web.controller;

import com.loja.web.dto.ClienteForm;
import com.loja.web.dto.EstoqueForm;
import com.loja.web.dto.PedidoForm;
import com.loja.web.dto.PedidoItemForm;
import com.loja.web.service.LojaClientService;
import com.loja.web.service.LojaClientService.WebClientException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {

    private final LojaClientService lojaClientService;

    public WebController(LojaClientService lojaClientService) {
        this.lojaClientService = lojaClientService;
    }

    @GetMapping("/")
    public String index(Model model) {
        if (!model.containsAttribute("clienteForm")) {
            model.addAttribute("clienteForm", new ClienteForm());
        }
        if (!model.containsAttribute("estoqueForm")) {
            model.addAttribute("estoqueForm", new EstoqueForm());
        }
        if (!model.containsAttribute("pedidoForm")) {
            model.addAttribute("pedidoForm", novoPedidoForm());
        }

        try {
            carregarDados(model);
        } catch (WebClientException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("clientes", java.util.List.of());
            model.addAttribute("produtos", java.util.List.of());
            model.addAttribute("pedidos", java.util.List.of());
        }
        return "index";
    }

    @PostMapping("/clientes")
    public String criarCliente(
            @Valid @ModelAttribute("clienteForm") ClienteForm clienteForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            garantirFormularios(model, clienteForm, new EstoqueForm(), novoPedidoForm());
            return "index";
        }

        try {
            lojaClientService.criarCliente(clienteForm);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente cadastrado com sucesso.");
            return "redirect:/";
        } catch (WebClientException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            garantirFormularios(model, clienteForm, new EstoqueForm(), novoPedidoForm());
            return "index";
        }
    }

    @PostMapping("/estoques")
    public String criarProduto(
            @Valid @ModelAttribute("estoqueForm") EstoqueForm estoqueForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            garantirFormularios(model, new ClienteForm(), estoqueForm, novoPedidoForm());
            return "index";
        }

        try {
            lojaClientService.criarProduto(estoqueForm);
            redirectAttributes.addFlashAttribute("successMessage", "Produto cadastrado no estoque.");
            return "redirect:/";
        } catch (WebClientException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            garantirFormularios(model, new ClienteForm(), estoqueForm, novoPedidoForm());
            return "index";
        }
    }

    @PostMapping("/pedidos")
    public String criarPedido(
            @Valid @ModelAttribute("pedidoForm") PedidoForm pedidoForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        normalizarItens(pedidoForm);

        if (bindingResult.hasErrors()) {
            garantirFormularios(model, new ClienteForm(), new EstoqueForm(), pedidoForm);
            return "index";
        }

        try {
            lojaClientService.criarPedido(pedidoForm);
            redirectAttributes.addFlashAttribute("successMessage", "Pedido criado com sucesso.");
            return "redirect:/";
        } catch (WebClientException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            garantirFormularios(model, new ClienteForm(), new EstoqueForm(), pedidoForm);
            return "index";
        }
    }

    @PostMapping("/pedidos/{id}/concluir")
    public String concluirPedido(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            lojaClientService.concluirPedido(id);
            redirectAttributes.addFlashAttribute("successMessage", "Pedido enviado para processamento de estoque.");
        } catch (WebClientException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/";
    }

    private void carregarDados(Model model) {
        model.addAttribute("clientes", lojaClientService.listarClientes());
        model.addAttribute("produtos", lojaClientService.listarProdutos());
        model.addAttribute("pedidos", lojaClientService.listarPedidos());
    }

    private void garantirFormularios(Model model, ClienteForm clienteForm, EstoqueForm estoqueForm, PedidoForm pedidoForm) {
        model.addAttribute("clienteForm", clienteForm);
        model.addAttribute("estoqueForm", estoqueForm);
        model.addAttribute("pedidoForm", pedidoForm);
        carregarDados(model);
    }

    private PedidoForm novoPedidoForm() {
        PedidoForm form = new PedidoForm();
        if (form.getItens().isEmpty()) {
            form.getItens().add(new PedidoItemForm());
        }
        return form;
    }

    private void normalizarItens(PedidoForm pedidoForm) {
        pedidoForm.setItens(
                pedidoForm.getItens().stream()
                        .filter(item -> item.getProdutoId() != null || item.getQuantidade() != null)
                        .toList()
        );
    }
}
