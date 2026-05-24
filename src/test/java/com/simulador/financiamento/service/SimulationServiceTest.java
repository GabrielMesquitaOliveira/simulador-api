package com.simulador.financiamento.service;

import com.simulador.financiamento.domain.validation.DomainValidationException;
import com.simulador.financiamento.repository.SimulationEntity;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SimulationServiceTest {

    @Inject
    SimulationService service;

    @Inject
    MeterRegistry registry;

    @Test
    @DisplayName("Deve executar uma simulação válida, persistir no banco e registrar métricas de observabilidade")
    void deveExecutarSimulacaoEPersistirComSucesso() {
        // Arrange
        BigDecimal principal = new BigDecimal("25000.00");
        BigDecimal interestRatePercent = new BigDecimal("2.5");
        int durationMonths = 10;

        double initialSuccessCount = registry.counter("simulations_requested_total", "status", "success").count();

        // Act
        SimulationEntity result = service.simulateAndSave(principal, interestRatePercent, durationMonths);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(0, result.getPrincipalAmount().compareTo(principal));
        assertEquals(0, result.getInterestRate().compareTo(new BigDecimal("0.02500000")));
        assertEquals(durationMonths, result.getDurationMonths());
        assertNotNull(result.getSteps());
        assertEquals(durationMonths, result.getSteps().size());
        
        // Verifica se realmente foi salvo no banco buscando pelo ID gerado
        SimulationEntity retrieved = service.findById(result.getId());
        assertNotNull(retrieved);
        assertEquals(result.getId(), retrieved.getId());

        // Asserções de Observabilidade Customizada
        double successCountAfter = registry.counter("simulations_requested_total", "status", "success").count();
        assertEquals(initialSuccessCount + 1, successCountAfter);

        // Verifica se os outros instrumentos de métricas foram registrados e receberam valores
        assertNotNull(registry.summary("simulation_principal_brl"));
        assertNotNull(registry.summary("simulation_duration_months"));
        assertNotNull(registry.timer("simulation_calculation_duration_seconds"));

        // O Gauge deve ser maior que zero (ou possuir um valor válido correspondente)
        io.micrometer.core.instrument.Gauge avgRateGauge = registry.find("simulation_average_interest_rate_percent").gauge();
        assertNotNull(avgRateGauge);
        assertTrue(avgRateGauge.value() > 0.0);
    }

    @Test
    @DisplayName("Deve recuperar uma simulação existente mapeando perfeitamente")
    void deveRecuperarSimulacaoExistente() {
        // Arrange
        BigDecimal principal = new BigDecimal("12000.00");
        BigDecimal interestRatePercent = new BigDecimal("1.25");
        int duration = 6;

        SimulationEntity created = service.simulateAndSave(principal, interestRatePercent, duration);
        assertNotNull(created);
        String id = created.getId();

        // Act
        SimulationEntity retrieved = service.findById(id);

        // Assert
        assertNotNull(retrieved);
        assertEquals(id, retrieved.getId());
        assertEquals(0, retrieved.getPrincipalAmount().compareTo(principal));
        assertEquals(0, retrieved.getInterestRate().compareTo(new BigDecimal("0.01250000")));
        assertEquals(duration, retrieved.getDurationMonths());
    }

    @Test
    @DisplayName("Deve lançar exceção SimulationNotFoundException ao buscar simulação inexistente")
    void deveLancarExcecaoAoBuscarSimulacaoInexistente() {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();

        // Act & Assert
        SimulationNotFoundException exception = assertThrows(
            SimulationNotFoundException.class,
            () -> service.findById(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("Simulação não encontrada com o ID: " + nonExistentId));
    }

    @ParameterizedTest
    @CsvSource({
        "-1000.00, 1.5, 12, 'O valor monetário não pode ser negativo'",
        "10000.00, -1.0, 12, 'A taxa de juros não pode ser negativa'",
        "10000.00, 1.5, 0, 'O prazo em meses deve ser maior que zero'",
        "10000.00, 1.5, -5, 'O prazo em meses deve ser maior que zero'"
    })
    @DisplayName("Deve propagar exceções de validação de domínio de forma Fail-Fast e registrar métrica de falha")
    void devePropagarExcecaoDeDominioAoSimularValoresInvalidos(
            BigDecimal principal, 
            BigDecimal interestRatePercent, 
            int durationMonths, 
            String expectedMessagePart
    ) {
        // Arrange
        double initialFailedCount = registry.counter("simulations_requested_total", "status", "validation_failed").count();

        // Act & Assert
        DomainValidationException exception = assertThrows(
            DomainValidationException.class,
            () -> service.simulateAndSave(principal, interestRatePercent, durationMonths)
        );

        assertTrue(
            exception.getMessage().toLowerCase().contains(expectedMessagePart.toLowerCase()),
            "Mensagem esperada conter: " + expectedMessagePart + " mas foi: " + exception.getMessage()
        );

        // Asserção de métrica de validação de domínio
        double failedCountAfter = registry.counter("simulations_requested_total", "status", "validation_failed").count();
        assertEquals(initialFailedCount + 1, failedCountAfter);
    }
}
