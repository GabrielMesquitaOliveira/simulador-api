package com.simulador.financiamento.service;

import com.simulador.financiamento.domain.shared.InterestRate;
import com.simulador.financiamento.domain.shared.Money;
import com.simulador.financiamento.domain.simulation.CompoundInterestStrategy;
import com.simulador.financiamento.domain.simulation.Simulation;
import com.simulador.financiamento.repository.SimulationEntity;
import com.simulador.financiamento.repository.SimulationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Serviço de aplicação encarregado de orquestrar os casos de uso de simulação
 * de financiamentos. Gerencia transações e atua como ponte entre o domínio puro
 * e a infraestrutura de persistência.
 */
@ApplicationScoped
public class SimulationService {

    @Inject
    SimulationRepository repository;

    /**
     * Orquestra a simulação de um financiamento utilizando juros compostos e
     * persiste o agregado correspondente no banco de dados.
     * 
     * @param principal O valor de principal solicitado.
     * @param interestRatePercent A taxa de juros percentual ao mês (ex: 1.5 para 1.5%).
     * @param durationMonths A duração total do financiamento em meses.
     * @return A entidade persistida {@link SimulationEntity} contendo os dados e a memória de cálculo.
     */
    @Transactional
    public SimulationEntity simulateAndSave(BigDecimal principal, BigDecimal interestRatePercent, int durationMonths) {
        // Converte e valida de forma Fail-Fast os parâmetros brutos em Value Objects de domínio
        Money moneyPrincipal = new Money(principal);
        InterestRate rate = InterestRate.fromPercentual(interestRatePercent);

        // Executa o cálculo financeiro no agregado de domínio rico
        Simulation domainSimulation = Simulation.execute(
            moneyPrincipal, 
            rate, 
            durationMonths, 
            new CompoundInterestStrategy()
        );

        // Converte o agregado de domínio para a entidade de persistência JPA
        SimulationEntity entity = SimulationEntity.fromDomain(domainSimulation);
        
        // Gera e atribui o identificador exclusivo (UUID) para a simulação pai
        String generatedId = UUID.randomUUID().toString();
        entity.setId(generatedId);

        // Persiste o agregado relacional completo (com passos em cascata) no banco H2
        repository.persist(entity);

        return entity;
    }

    /**
     * Busca uma simulação persistida pelo seu identificador exclusivo.
     * 
     * @param id O UUID da simulação em formato String.
     * @return A entidade persistida {@link SimulationEntity} localizada.
     * @throws SimulationNotFoundException se nenhuma simulação for localizada com o ID fornecido.
     */
    public SimulationEntity findById(String id) {
        SimulationEntity entity = repository.findById(id);
        if (entity == null) {
            throw new SimulationNotFoundException("Simulação não encontrada com o ID: " + id);
        }
        return entity;
    }
}
