package com.simulador.financiamento.service;

import com.simulador.financiamento.domain.validation.DomainValidationException;
import com.simulador.financiamento.repository.SimulationEntity;
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

    @Test
    @DisplayName("Deve executar uma simulação válida, persistir no banco e retornar a entidade correspondente")
    void deveExecutarSimulacaoEPersistirComSucesso() {
        // Arrange
        BigDecimal principal = new BigDecimal("25000.00");
        BigDecimal interestRatePercent = new BigDecimal("2.5");
        int durationMonths = 10;

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
    @DisplayName("Deve propagar exceções de validação de domínio de forma Fail-Fast")
    void devePropagarExcecaoDeDominioAoSimularValoresInvalidos(
            BigDecimal principal, 
            BigDecimal interestRatePercent, 
            int durationMonths, 
            String expectedMessagePart
    ) {
        // Act & Assert
        DomainValidationException exception = assertThrows(
            DomainValidationException.class,
            () -> service.simulateAndSave(principal, interestRatePercent, durationMonths)
        );

        assertTrue(
            exception.getMessage().toLowerCase().contains(expectedMessagePart.toLowerCase()),
            "Mensagem esperada conter: " + expectedMessagePart + " mas foi: " + exception.getMessage()
        );
    }
}
