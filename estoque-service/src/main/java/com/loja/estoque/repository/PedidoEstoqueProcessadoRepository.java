package com.loja.estoque.repository;

import com.loja.estoque.entity.PedidoEstoqueProcessado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoEstoqueProcessadoRepository extends JpaRepository<PedidoEstoqueProcessado, Long> {

    boolean existsByPedidoId(Long pedidoId);
}
