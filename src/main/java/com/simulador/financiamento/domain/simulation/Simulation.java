package com.simulador.financiamento.domain.simulation;

import com.simulador.financiamento.domain.shared.InterestRate;
import com.simulador.financiamento.domain.shared.Money;
import com.simulador.financiamento.domain.validation.DomainValidator;
import java.math.BigDecimal;
import java.util.List;

/**
 * Entidade Raiz do Agregado de Simulação (Aggregate Root), imutável e encapsulada em um {@code record}.
 * Centraliza e coordena o estado final do financiamento simulado, bem como a sua memória de cálculo evolutiva.
 * 
 * @param principal O valor inicial simulado (saldo devedor original).
 * @param rate A taxa de juros aplicada periodicamente à simulação.
 * @param durationMonths O período de vigência da simulação em meses.
 * @param finalBalance O montante consolidado do saldo devedor ao final de todos os meses de vigência.
 * @param totalInterest O somatório total de juros incidentes e acumulados ao longo do financiamento.
 * @param steps Lista detalhada contendo cada etapa ou passo mensal da simulação (memória de cálculo).
 */
public record Simulation(
    Money principal,
    InterestRate rate,
    int durationMonths,
    Money finalBalance,
    Money totalInterest,
    List<SimulationStep> steps
) {

    /**
     * Construtor compacto para validar e normalizar o estado consolidado da simulação.
     * Além de realizar os testes de nulidade e positividade de prazo através do {@link DomainValidator},
     * gera uma cópia defensiva imutável da lista de passos (steps) para preservar a encapsulação do Agregado.
     * 
     * @throws DomainValidationException Se algum atributo obrigatório for nulo ou se o prazo for menor ou igual a zero.
     */
    public Simulation {
        DomainValidator.requireNonNull(principal, "O saldo principal não pode ser nulo.");
        DomainValidator.requireNonNull(rate, "A taxa de juros não pode ser nula.");
        DomainValidator.requirePositive(durationMonths, "O prazo em meses deve ser maior que zero.");
        DomainValidator.requireNonNull(finalBalance, "O saldo final não pode ser nulo.");
        DomainValidator.requireNonNull(totalInterest, "O total de juros não pode ser nulo.");
        DomainValidator.requireNonNull(steps, "Os passos da simulação não podem ser nulos.");
        steps = List.copyOf(steps);
    }

    /**
     * Ponto de entrada (Factory Method) para execução de uma nova simulação financeira.
     * Utiliza a estratégia de cálculo informada para simular a evolução e calcular os totais consolidados.
     * 
     * @param principal O montante original a ser simulado.
     * @param rate A taxa de juros a ser aplicada.
     * @param durationMonths O prazo da simulação em meses.
     * @param strategy A estratégia de amortização/juros a ser empregada no cálculo.
     * @return Uma instância consolidada e imutável de {@link Simulation}.
     * @throws DomainValidationException Se algum dos parâmetros for nulo ou se o prazo for inválido.
     */
    public static Simulation execute(Money principal, InterestRate rate, int durationMonths, InterestCalculationStrategy strategy) {
        DomainValidator.requireNonNull(strategy, "A estratégia de cálculo não pode ser nula.");
        
        List<SimulationStep> steps = strategy.calculate(principal, rate, durationMonths);
        
        Money finalBalance = steps.isEmpty() ? principal : steps.get(steps.size() - 1).finalBalance();
        
        Money totalInterest = steps.stream()
            .map(SimulationStep::interestAmount)
            .reduce(new Money(BigDecimal.ZERO), Money::add);

        return new Simulation(principal, rate, durationMonths, finalBalance, totalInterest, steps);
    }
}
