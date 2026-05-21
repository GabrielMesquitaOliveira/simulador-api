package com.simulador.financiamento.repository;

import com.simulador.financiamento.domain.shared.InterestRate;
import com.simulador.financiamento.domain.shared.Money;
import com.simulador.financiamento.domain.simulation.CompoundInterestStrategy;
import com.simulador.financiamento.domain.simulation.Simulation;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SimulationRepositoryTest {

    @Inject
    SimulationRepository repository;

    @Test
    @DisplayName("Deve persistir e recuperar uma simulação completa com seus passos de cálculo")
    @Transactional
    void devePersistirERecuperarSimulacaoCompleta() {
        // 1. Arrange - Cria dados de simulação no domínio
        Money principal = new Money(new BigDecimal("10000.00"));
        InterestRate rate = InterestRate.fromPercentual(new BigDecimal("1.5"));
        int duration = 12;
        
        Simulation domainSimulation = Simulation.execute(principal, rate, duration, new CompoundInterestStrategy());
        
        // 2. Act - Mapeia para entidade e persiste
        SimulationEntity entity = SimulationEntity.fromDomain(domainSimulation);
        String generatedId = UUID.randomUUID().toString();
        entity.setId(generatedId);
        
        repository.persist(entity);
        repository.flush();
        
        // Limpa o persistence context do Hibernate para forçar a leitura física do banco
        repository.getEntityManager().clear();
        
        // 3. Assert - Recupera do banco e valida integridade
        SimulationEntity retrievedEntity = repository.findById(generatedId);
        assertNotNull(retrievedEntity);
        assertEquals(generatedId, retrievedEntity.getId());
        assertEquals(0, retrievedEntity.getPrincipalAmount().compareTo(new BigDecimal("10000.00")));
        assertEquals(0, retrievedEntity.getInterestRate().compareTo(new BigDecimal("0.01500000")));
        assertEquals(12, retrievedEntity.getDurationMonths());
        
        // Valida as parcelas cascade
        assertNotNull(retrievedEntity.getSteps());
        assertEquals(12, retrievedEntity.getSteps().size());
        
        // Valida a conversão de volta para o domínio
        Simulation reconstructedDomain = retrievedEntity.toDomain();
        assertNotNull(reconstructedDomain);
        assertTrue(reconstructedDomain.principal().isEqualTo(principal));
        assertTrue(reconstructedDomain.rate().decimalValue().compareTo(rate.decimalValue()) == 0);
        assertEquals(duration, reconstructedDomain.durationMonths());
        assertEquals(12, reconstructedDomain.steps().size());
        assertTrue(reconstructedDomain.totalInterest().isGreaterThan(new Money(BigDecimal.ZERO)));
    }

    @Test
    @DisplayName("Deve remover os passos da simulação em cascata ao deletar a simulação")
    @Transactional
    void deveRemoverPassosEmCascataAoDeletarSimulacao() {
        // 1. Arrange
        Money principal = new Money(new BigDecimal("5000.00"));
        InterestRate rate = InterestRate.fromPercentual(new BigDecimal("2.0"));
        Simulation domainSimulation = Simulation.execute(principal, rate, 6, new CompoundInterestStrategy());
        
        SimulationEntity entity = SimulationEntity.fromDomain(domainSimulation);
        String id = UUID.randomUUID().toString();
        entity.setId(id);
        
        repository.persist(entity);
        repository.flush();
        
        // Verifica que os passos foram gravados no banco
        long stepCountBeforeDelete = repository.getEntityManager()
            .createQuery("SELECT COUNT(s) FROM SimulationStepEntity s WHERE s.simulation.id = :simId", Long.class)
            .setParameter("simId", id)
            .getSingleResult();
        assertEquals(6, stepCountBeforeDelete);
        
        // 2. Act - Remove a simulação principal
        repository.delete(entity);
        repository.flush();
        
        // 3. Assert - Garante que os passos filhos foram deletados por CASCADE
        long stepCountAfterDelete = repository.getEntityManager()
            .createQuery("SELECT COUNT(s) FROM SimulationStepEntity s WHERE s.simulation.id = :simId", Long.class)
            .setParameter("simId", id)
            .getSingleResult();
        assertEquals(0, stepCountAfterDelete);
    }
}
