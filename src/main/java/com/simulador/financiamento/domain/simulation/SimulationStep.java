package com.simulador.financiamento.domain.simulation;

import com.simulador.financiamento.domain.shared.Money;
import com.simulador.financiamento.domain.validation.DomainValidator;

/**
 * Representa uma etapa da evolução mensal da simulação de financiamento (uma linha da memória de cálculo).
 * É uma entidade local do Agregado de Simulação, implementada como um {@code record} imutável.
 * Contém a validação de consistência matemática que impede divergências de arredondamento.
 * 
 * @param month O número ordinal do mês da simulação (1-indexed).
 * @param initialBalance O saldo devedor inicial no começo deste mês específico.
 * @param interestAmount O montante de juros calculado e incidido durante este mês.
 * @param finalBalance O saldo devedor final ao término deste mês (saldo inicial + juros).
 */
public record SimulationStep(int month, Money initialBalance, Money interestAmount, Money finalBalance) {

    /**
     * Construtor compacto para validação das invariantes de cada passo da simulação.
     * Além de verificar valores não-nulos e prazos positivos, valida estritamente a
     * coerência aritmética do passo: o saldo final deve ser exatamente igual ao saldo inicial somado aos juros.
     * 
     * @throws DomainValidationException Se algum parâmetro for nulo, se month for <= 0, ou se a soma matemática falhar na igualdade.
     */
    public SimulationStep {
        DomainValidator.requirePositive(month, "O número do mês deve ser maior que zero.");
        DomainValidator.requireNonNull(initialBalance, "O saldo inicial não pode ser nulo.");
        DomainValidator.requireNonNull(interestAmount, "O valor dos juros não pode ser nulo.");
        DomainValidator.requireNonNull(finalBalance, "O saldo final não pode ser nulo.");
        
        // Verifica a coerência matemática: finalBalance == initialBalance + interestAmount
        Money expectedFinalBalance = initialBalance.add(interestAmount);
        DomainValidator.requireTrue(
            finalBalance.isEqualTo(expectedFinalBalance),
            "O saldo final deve ser igual ao saldo inicial somado aos juros."
        );
    }
}
