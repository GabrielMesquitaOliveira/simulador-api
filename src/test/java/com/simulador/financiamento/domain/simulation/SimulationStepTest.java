package com.simulador.financiamento.domain.simulation;

import com.simulador.financiamento.domain.shared.Money;
import com.simulador.financiamento.domain.validation.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SimulationStepTest {

    @Test
    @DisplayName("Deve criar SimulationStep com valores validos")
    void deveCriarSimulationStepComValoresValidos() {
        Money initial = new Money(new BigDecimal("1000.00"));
        Money interest = new Money(new BigDecimal("15.00"));
        Money fin = new Money(new BigDecimal("1015.00"));
        
        SimulationStep step = new SimulationStep(1, initial, interest, fin);
        
        assertEquals(1, step.month());
        assertEquals(initial, step.initialBalance());
        assertEquals(interest, step.interestAmount());
        assertEquals(fin, step.finalBalance());
    }

    @Test
    @DisplayName("Deve lancar excecao se o mes for zero ou negativo")
    void deveLancarExcecaoSeMesInvalido() {
        Money initial = new Money(new BigDecimal("1000.00"));
        Money interest = new Money(new BigDecimal("15.00"));
        Money fin = new Money(new BigDecimal("1015.00"));

        DomainValidationException ex0 = assertThrows(DomainValidationException.class, () -> {
            new SimulationStep(0, initial, interest, fin);
        });
        assertEquals("O número do mês deve ser maior que zero.", ex0.getMessage());

        DomainValidationException exNeg = assertThrows(DomainValidationException.class, () -> {
            new SimulationStep(-1, initial, interest, fin);
        });
        assertEquals("O número do mês deve ser maior que zero.", exNeg.getMessage());
    }

    @Test
    @DisplayName("Deve lancar excecao se algum valor monetario for nulo")
    void deveLancarExcecaoSeValorNulo() {
        Money valid = new Money(new BigDecimal("1000.00"));

        assertThrows(DomainValidationException.class, () -> {
            new SimulationStep(1, null, valid, valid);
        });
        assertThrows(DomainValidationException.class, () -> {
            new SimulationStep(1, valid, null, valid);
        });
        assertThrows(DomainValidationException.class, () -> {
            new SimulationStep(1, valid, valid, null);
        });
    }

    @Test
    @DisplayName("Deve lancar excecao se a coerencia matematica nao for respeitada")
    void deveLancarExcecaoSeCoerenciaMatematicaNaoRespeitada() {
        Money initial = new Money(new BigDecimal("1000.00"));
        Money interest = new Money(new BigDecimal("15.00"));
        Money invalidFinal = new Money(new BigDecimal("1020.00"));

        DomainValidationException ex = assertThrows(DomainValidationException.class, () -> {
            new SimulationStep(1, initial, interest, invalidFinal);
        });
        assertEquals("O saldo final deve ser igual ao saldo inicial somado aos juros.", ex.getMessage());
    }
}
