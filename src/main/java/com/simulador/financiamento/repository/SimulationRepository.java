package com.simulador.financiamento.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repositório Panache encarregado do acesso a dados e persistência do Aggregate Root
 * de simulação ({@link SimulationEntity}). Implementa a interface {@link PanacheRepositoryBase}
 * para gerenciar operações CRUD, pesquisas e manipulações de registros H2 de forma nativa e limpa
 * com chave primária do tipo String (UUID).
 */
@ApplicationScoped
public class SimulationRepository implements PanacheRepositoryBase<SimulationEntity, String> {
    // Customizações ou consultas HQL específicas do repositório podem ser inseridas aqui
}
