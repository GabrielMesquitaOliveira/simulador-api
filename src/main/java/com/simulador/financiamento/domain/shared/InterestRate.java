package com.simulador.financiamento.domain.shared;

import com.simulador.financiamento.domain.validation.DomainValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Objeto de Valor (Value Object) que representa a Taxa de Juros de um financiamento.
 * Armazena e manipula a taxa internamente sob a forma decimal com precisão estrita de
 * **8 casas decimais** e arredondamento **HALF_EVEN**, assegurando total conformidade
 * com as normativas do Banco Central do Brasil (BACEn).
 * 
 * @param decimalValue O valor decimal da taxa de juros (ex: 0.01500000 para uma taxa de 1.50%).
 */
public record InterestRate(BigDecimal decimalValue) {

    /**
     * Construtor compacto para validação e padronização da taxa.
     * Garante que a taxa não seja negativa e define a escala de 8 casas decimais.
     * 
     * @throws DomainValidationException Se o valor decimalValue for nulo ou negativo.
     */
    public InterestRate {
        DomainValidator.requireNonNegative(decimalValue, "A taxa de juros não pode ser negativa.");
        // Garante 8 casas decimais de precisão conforme as regras do BACEn
        decimalValue = decimalValue.setScale(8, RoundingMode.HALF_EVEN);
    }

    /**
     * Cria uma instância de InterestRate a partir de um valor percentual.
     * Exemplo: um percentual de 1.5 é convertido internamente para 0.01500000 (dividido por 100).
     * 
     * @param percentual O valor da taxa em percentual (ex: 1.5).
     * @return Uma nova instância de {@link InterestRate} contendo o correspondente valor decimal.
     * @throws DomainValidationException Se o percentual for nulo ou negativo.
     */
    public static InterestRate fromPercentual(BigDecimal percentual) {
        DomainValidator.requireNonNegative(percentual, "A taxa de juros não pode ser negativa.");
        // Converte o percentual (ex: 1.5) dividindo por 100 para decimal (ex: 0.01500000)
        BigDecimal decimal = percentual.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_EVEN);
        return new InterestRate(decimal);
    }
}
