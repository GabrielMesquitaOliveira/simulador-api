package com.simulador.financiamento.domain.shared;

import com.simulador.financiamento.domain.validation.DomainValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Objeto de Valor (Value Object) que encapsula um valor monetário de forma imutável.
 * Impede o uso de tipos primitivos de ponto flutuante (double/float) para evitar erros
 * de precisão centesimal. Todas as quantias de dinheiro são normalizadas para **2 casas decimais**
 * e arredondadas comercialmente utilizando **HALF_EVEN**.
 * 
 * @param amount O valor numérico em {@link BigDecimal} que representa a quantia monetária.
 */
public record Money(BigDecimal amount) implements Comparable<Money> {

    /**
     * Construtor compacto para validar e padronizar o valor monetário.
     * Assegura que o montante não seja negativo e define a escala padrão de 2 casas decimais.
     * 
     * @throws DomainValidationException Se o amount for nulo ou negativo.
     */
    public Money {
        DomainValidator.requireNonNegative(amount, "O valor monetário não pode ser negativo.");
        // Garante que a escala interna seja sempre 2 com arredondamento HALF_EVEN
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Soma este valor monetário com outro.
     * 
     * @param other O outro valor monetário a ser somado.
     * @return Um novo {@link Money} representando a soma.
     * @throws DomainValidationException Se o valor fornecido for nulo.
     */
    public Money add(Money other) {
        DomainValidator.requireNonNull(other, "O valor monetário para soma não pode ser nulo.");
        return new Money(this.amount.add(other.amount()));
    }

    /**
     * Multiplica este valor monetário por um fator multiplicativo (ex: taxa decimal).
     * 
     * @param factor O fator de multiplicação.
     * @return Um novo {@link Money} contendo o resultado da operação.
     * @throws DomainValidationException Se o fator for nulo.
     */
    public Money multiply(BigDecimal factor) {
        DomainValidator.requireNonNull(factor, "O fator de multiplicação não pode ser nulo.");
        BigDecimal result = this.amount.multiply(factor);
        return new Money(result);
    }

    /**
     * Verifica se este valor monetário é estritamente menor que outro.
     * 
     * @param other O outro valor monetário para comparação.
     * @return {@code true} se este for menor que {@code other}, senão {@code false}.
     */
    public boolean isLessThan(Money other) {
        return this.compareTo(other) < 0;
    }

    /**
     * Verifica se este valor monetário é estritamente maior que outro.
     * 
     * @param other O outro valor monetário para comparação.
     * @return {@code true} se este for maior que {@code other}, senão {@code false}.
     */
    public boolean isGreaterThan(Money other) {
        return this.compareTo(other) > 0;
    }

    /**
     * Verifica se este valor monetário é numericamente equivalente a outro.
     * 
     * @param other O outro valor monetário para comparação.
     * @return {@code true} se ambos forem iguais na escala monetária, senão {@code false}.
     */
    public boolean isEqualTo(Money other) {
        return this.compareTo(other) == 0;
    }

    /**
     * Compara este valor monetário com outro para fins de ordenação ou igualdade numérica.
     * 
     * @param other O outro valor monetário a ser comparado.
     * @return Um valor negativo, zero ou um valor positivo dependendo da relação numérica.
     * @throws DomainValidationException Se o outro valor for nulo.
     */
    @Override
    public int compareTo(Money other) {
        DomainValidator.requireNonNull(other, "O valor monetário para comparação não pode ser nulo.");
        return this.amount.compareTo(other.amount());
    }
}
