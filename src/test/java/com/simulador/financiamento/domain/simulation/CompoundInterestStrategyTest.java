package com.simulador.financiamento.domain.simulation;

import com.simulador.financiamento.domain.shared.InterestRate;
import com.simulador.financiamento.domain.shared.Money;
import com.simulador.financiamento.domain.validation.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompoundInterestStrategyTest {

    private final InterestCalculationStrategy strategy = new CompoundInterestStrategy();

    @Test
    @DisplayName("Deve calcular juros compostos para 1 mes corretamente")
    void deveCalcularJurosCompostosParaUmMes() {
        Money principal = new Money(new BigDecimal("1000.00"));
        InterestRate rate = InterestRate.fromPercentual(new BigDecimal("1.5")); // 1.5% -> 0.015
        
        List<SimulationStep> steps = strategy.calculate(principal, rate, 1);
        
        assertEquals(1, steps.size());
        SimulationStep step1 = steps.get(0);
        
        assertEquals(1, step1.month());
        assertEquals(new Money(new BigDecimal("1000.00")), step1.initialBalance());
        assertEquals(new Money(new BigDecimal("15.00")), step1.interestAmount());
        assertEquals(new Money(new BigDecimal("1015.00")), step1.finalBalance());
    }

    @Test
    @DisplayName("Deve calcular juros compostos para multiplos meses com juros sobre juros")
    void deveCalcularJurosCompostosParaMultiplosMeses() {
        Money principal = new Money(new BigDecimal("1000.00"));
        InterestRate rate = InterestRate.fromPercentual(new BigDecimal("10.0")); // 10%
        
        List<SimulationStep> steps = strategy.calculate(principal, rate, 3);
        
        assertEquals(3, steps.size());
        
        // Mes 1: 1000.00 -> juros 100.00 -> final 1100.00
        SimulationStep step1 = steps.get(0);
        assertEquals(1, step1.month());
        assertEquals(new Money(new BigDecimal("1000.00")), step1.initialBalance());
        assertEquals(new Money(new BigDecimal("100.00")), step1.interestAmount());
        assertEquals(new Money(new BigDecimal("1100.00")), step1.finalBalance());

        // Mes 2: 1100.00 -> juros 110.00 -> final 1210.00
        SimulationStep step2 = steps.get(1);
        assertEquals(2, step2.month());
        assertEquals(new Money(new BigDecimal("1100.00")), step2.initialBalance());
        assertEquals(new Money(new BigDecimal("110.00")), step2.interestAmount());
        assertEquals(new Money(new BigDecimal("1210.00")), step2.finalBalance());

        // Mes 3: 1210.00 -> juros 121.00 -> final 1331.00
        SimulationStep step3 = steps.get(2);
        assertEquals(3, step3.month());
        assertEquals(new Money(new BigDecimal("1210.00")), step3.initialBalance());
        assertEquals(new Money(new BigDecimal("121.00")), step3.interestAmount());
        assertEquals(new Money(new BigDecimal("1331.00")), step3.finalBalance());
    }

    @Test
    @DisplayName("Deve retornar saldo constante com taxa de juros 0%")
    void deveRetornarSaldoConstanteComTaxaZero() {
        Money principal = new Money(new BigDecimal("1500.00"));
        InterestRate rate = InterestRate.fromPercentual(BigDecimal.ZERO);
        
        List<SimulationStep> steps = strategy.calculate(principal, rate, 5);
        
        assertEquals(5, steps.size());
        for (int i = 0; i < 5; i++) {
            SimulationStep step = steps.get(i);
            assertEquals(i + 1, step.month());
            assertEquals(principal, step.initialBalance());
            assertEquals(new Money(BigDecimal.ZERO), step.interestAmount());
            assertEquals(principal, step.finalBalance());
        }
    }

    @Test
    @DisplayName("Deve lancar excecao se os parametros forem invalidos")
    void deveLancarExcecaoSeParametrosInvalidos() {
        Money principal = new Money(new BigDecimal("1000.00"));
        InterestRate rate = InterestRate.fromPercentual(new BigDecimal("1.5"));

        assertThrows(DomainValidationException.class, () -> {
            strategy.calculate(null, rate, 12);
        });

        assertThrows(DomainValidationException.class, () -> {
            strategy.calculate(principal, null, 12);
        });

        assertThrows(DomainValidationException.class, () -> {
            strategy.calculate(principal, rate, 0);
        });

        assertThrows(DomainValidationException.class, () -> {
            strategy.calculate(principal, rate, -5);
        });
    }
}
