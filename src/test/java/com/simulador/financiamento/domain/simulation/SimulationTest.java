package com.simulador.financiamento.domain.simulation;

import com.simulador.financiamento.domain.shared.InterestRate;
import com.simulador.financiamento.domain.shared.Money;
import com.simulador.financiamento.domain.validation.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SimulationTest {

    @Test
    @DisplayName("Deve criar Simulation e calcular com sucesso usando a estrategia")
    void deveCriarSimulationECalcularComSucesso() {
        Money principal = new Money(new BigDecimal("1000.00"));
        InterestRate rate = InterestRate.fromPercentual(new BigDecimal("10.0")); // 10%
        int duration = 3;
        InterestCalculationStrategy strategy = new CompoundInterestStrategy();

        Simulation sim = Simulation.execute(principal, rate, duration, strategy);

        assertEquals(principal, sim.principal());
        assertEquals(rate, sim.rate());
        assertEquals(duration, sim.durationMonths());
        assertEquals(new Money(new BigDecimal("1331.00")), sim.finalBalance());
        assertEquals(new Money(new BigDecimal("331.00")), sim.totalInterest());
        assertEquals(3, sim.steps().size());
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar criar Simulation com parametros invalidos")
    void deveLancarExcecaoComParametrosInvalidos() {
        Money principal = new Money(new BigDecimal("1000.00"));
        InterestRate rate = InterestRate.fromPercentual(new BigDecimal("10.0"));
        InterestCalculationStrategy strategy = new CompoundInterestStrategy();

        // Saldo principal nulo
        assertThrows(DomainValidationException.class, () -> {
            Simulation.execute(null, rate, 3, strategy);
        });

        // Taxa nula
        assertThrows(DomainValidationException.class, () -> {
            Simulation.execute(principal, null, 3, strategy);
        });

        // Prazo negativo ou zero
        assertThrows(DomainValidationException.class, () -> {
            Simulation.execute(principal, rate, 0, strategy);
        });
        assertThrows(DomainValidationException.class, () -> {
            Simulation.execute(principal, rate, -1, strategy);
        });

        // Estrategia nula
        assertThrows(DomainValidationException.class, () -> {
            Simulation.execute(principal, rate, 3, null);
        });
    }

    @Test
    @DisplayName("Deve garantir que a lista de passos da simulação seja imutável")
    void deveGarantirQueListaDePassosSejaImutavel() {
        Money principal = new Money(new BigDecimal("1000.00"));
        InterestRate rate = InterestRate.fromPercentual(new BigDecimal("10.0"));
        InterestCalculationStrategy strategy = new CompoundInterestStrategy();

        Simulation sim = Simulation.execute(principal, rate, 3, strategy);

        assertThrows(UnsupportedOperationException.class, () -> {
            sim.steps().clear();
        });
    }
}
