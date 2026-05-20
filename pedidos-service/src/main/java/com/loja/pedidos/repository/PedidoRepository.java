package com.loja.pedidos.repository;

import com.loja.pedidos.entity.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Override
    @EntityGraph(attributePaths = "itens")
    java.util.List<Pedido> findAll();

    @Override
    @EntityGraph(attributePaths = "itens")
    java.util.Optional<Pedido> findById(Long id);
}
