package com.simulador.financiamento.domain.validation;

import java.math.BigDecimal;

/**
 * Utilitário de validação centralizado que implementa o padrão Assertion Concern.
 * Fornece métodos estáticos para validar as invariantes e regras de negócio
 * no momento da construção de objetos do domínio, garantindo que nenhum
 * estado inválido seja instanciado (mecanismo Fail-Fast).
 */
public final class DomainValidator {

    private DomainValidator() {
        // Impede instanciação
    }

    /**
     * Valida que o objeto fornecido não é nulo.
     * 
     * @param value O objeto a ser validado.
     * @param message A mensagem de erro caso o objeto seja nulo.
     * @throws DomainValidationException Se o valor for nulo.
     */
    public static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new DomainValidationException(message);
        }
    }

    /**
     * Valida que o valor decimal fornecido não é negativo (deve ser maior ou igual a zero).
     * 
     * @param value O BigDecimal a ser validado.
     * @param message A mensagem de erro caso o valor seja negativo ou nulo.
     * @throws DomainValidationException Se o valor for nulo ou negativo.
     */
    public static void requireNonNegative(BigDecimal value, String message) {
        requireNonNull(value, message);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException(message);
        }
    }

    /**
     * Valida que o número inteiro fornecido é estritamente positivo (maior que zero).
     * 
     * @param value O número inteiro a ser validado.
     * @param message A mensagem de erro caso o valor seja menor ou igual a zero.
     * @throws DomainValidationException Se o valor não for positivo.
     */
    public static void requirePositive(int value, String message) {
        if (value <= 0) {
            throw new DomainValidationException(message);
        }
    }

    /**
     * Valida que uma condição lógica seja verdadeira.
     * 
     * @param condition A condição lógica a ser validada.
     * @param message A mensagem de erro caso a condição seja falsa.
     * @throws DomainValidationException Se a condição for falsa.
     */
    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new DomainValidationException(message);
        }
    }
}
