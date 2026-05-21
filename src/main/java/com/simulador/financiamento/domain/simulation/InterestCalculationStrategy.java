package com.simulador.financiamento.domain.simulation;

import com.simulador.financiamento.domain.shared.InterestRate;
import com.simulador.financiamento.domain.shared.Money;
import java.util.List;

/**
 * Interface que define a estratégia algorítmica para o cálculo evolutivo de juros
 * de um financiamento ou investimento ao longo do tempo.
 * Implementa o padrão de projeto Strategy para permitir a fácil alternância entre
 * diferentes regimes de amortização ou juros (ex: juros compostos, juros simples, SAC, Price).
 */
public interface InterestCalculationStrategy {

    /**
     * Calcula a memória de cálculo evolutiva do financiamento mês a mês com base
     * na estratégia específica.
     * 
     * @param principal O valor inicial do saldo devedor (valor principal).
     * @param monthlyRate A taxa de juros aplicada mensalmente.
     * @param durationMonths O prazo total do financiamento em meses.
     * @return Uma lista imutável de {@link SimulationStep} contendo a evolução mensal detalhada do financiamento.
     * @throws DomainValidationException Se algum dos parâmetros de entrada for nulo, negativo ou inválido.
     */
    List<SimulationStep> calculate(Money principal, InterestRate monthlyRate, int durationMonths);
}
