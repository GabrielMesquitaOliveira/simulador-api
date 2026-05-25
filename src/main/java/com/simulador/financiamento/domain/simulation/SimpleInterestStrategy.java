package com.simulador.financiamento.domain.simulation;

import com.simulador.financiamento.domain.shared.InterestRate;
import com.simulador.financiamento.domain.shared.Money;
import com.simulador.financiamento.domain.validation.DomainValidator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementação da estratégia de juros simples para evolução de saldos.
 * Sob o regime de juros simples, a parcela de juros de cada período é calculada
 * exclusivamente sobre o capital inicial (principal) original e acumulada periodica e linearmente.
 */
public final class SimpleInterestStrategy implements InterestCalculationStrategy {

    /**
     * Gera a memória de cálculo mensal sob a sistemática de capitalização simples.
     * Mês a mês, calcula o juro fixo com base no principal original e acumula-o linearmente.
     * 
     * @param principal O valor inicial do financiamento ou aporte (capital inicial).
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
        
        // Em juros simples, o valor do juro de cada mês é constante e calculado sobre o principal original
        Money constantInterestAmount = principal.multiply(monthlyRate.decimalValue());

        for (int month = 1; month <= durationMonths; month++) {
            Money finalBalance = currentBalance.add(constantInterestAmount);
            steps.add(new SimulationStep(month, currentBalance, constantInterestAmount, finalBalance));
            currentBalance = finalBalance;
        }

        return Collections.unmodifiableList(steps);
    }
}
