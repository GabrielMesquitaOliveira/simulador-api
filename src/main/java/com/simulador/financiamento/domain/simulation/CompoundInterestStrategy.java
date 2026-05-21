package com.simulador.financiamento.domain.simulation;

import com.simulador.financiamento.domain.shared.InterestRate;
import com.simulador.financiamento.domain.shared.Money;
import com.simulador.financiamento.domain.validation.DomainValidator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementação da estratégia de juros compostos para evolução de saldos.
 * Aplica a fórmula clássica de capitalização sob o regime de juros compostos:
 * {@code M = C * (1 + i)^t}, onde os juros de cada período são capitalizados e acrescidos
 * ao saldo devedor/principal do período seguinte.
 */
public class CompoundInterestStrategy implements InterestCalculationStrategy {

    /**
     * Gera a memória de cálculo mensal sob a sistemática de capitalização composta.
     * Mês a mês, calcula o juro sobre o saldo inicial daquele período e gera
     * um novo saldo final imutável para servir de saldo inicial do período subsequente.
     * 
     * @param principal O valor inicial do financiamento ou aporte.
     * @param monthlyRate A taxa de juros a ser aplicada a cada período mensal.
     * @param durationMonths O número total de períodos (meses) da simulação.
     * @return Uma lista imutável com a memória de evolução detalhada contendo {@link SimulationStep}s.
     * @throws DomainValidationException Se principal ou monthlyRate forem nulos, ou se durationMonths for menor ou igual a zero.
     */
    @Override
    public List<SimulationStep> calculate(Money principal, InterestRate monthlyRate, int durationMonths) {
        DomainValidator.requireNonNull(principal, "O saldo principal não pode ser nulo.");
        DomainValidator.requireNonNull(monthlyRate, "A taxa de juros não pode ser nula.");
        DomainValidator.requirePositive(durationMonths, "O prazo em meses deve ser maior que zero.");

        List<SimulationStep> steps = new ArrayList<>();
        Money currentBalance = principal;

        for (int month = 1; month <= durationMonths; month++) {
            Money interestAmount = currentBalance.multiply(monthlyRate.decimalValue());
            Money finalBalance = currentBalance.add(interestAmount);

            steps.add(new SimulationStep(month, currentBalance, interestAmount, finalBalance));
            currentBalance = finalBalance;
        }

        return Collections.unmodifiableList(steps);
    }
}
