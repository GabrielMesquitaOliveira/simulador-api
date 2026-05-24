package com.simulador.financiamento.service;

import com.simulador.financiamento.repository.SimulationEntity;
import com.simulador.financiamento.repository.SimulationRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class SimulationServiceRetryTest {

    @Inject
    SimulationService service;

    @InjectMock
    SimulationRepository repository;

    @Test
    @DisplayName("Deve tentar persistir e recuperar com sucesso após falhas concorrentes transientes (comportamento do @Retry)")
    void deveRecuperarComRetryAposFalhasTransientes() {
        // Arrange
        BigDecimal principal = new BigDecimal("10000.00");
        BigDecimal interestRatePercent = new BigDecimal("1.5");
        int durationMonths = 12;

        // Configura o mock do repositório para lançar RuntimeException duas vezes
        // e depois persistir com sucesso (faz nada) na terceira tentativa.
        doThrow(new RuntimeException("Transient DB Lock"))
        .doThrow(new RuntimeException("Transient DB Lock"))
        .doNothing()
        .when(repository).persist(any(SimulationEntity.class));

        // Act
        SimulationEntity result = service.simulateAndSave(principal, interestRatePercent, durationMonths);

        // Assert
        assertNotNull(result);
        // Verifica se persist foi chamado exatamente 3 vezes (2 falhas + 1 sucesso devido à política de @Retry)
        verify(repository, times(3)).persist(any(SimulationEntity.class));
    }
}
