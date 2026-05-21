package com.simulador.financiamento.domain.shared;

import com.simulador.financiamento.domain.validation.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InterestRateTest {

    @Test
    @DisplayName("Deve criar InterestRate a partir de valor percentual valido com 8 casas decimais")
    void deveCriarInterestRateAPartirDePercentualValido() {
        BigDecimal percentual = new BigDecimal("1.5");
        InterestRate rate = InterestRate.fromPercentual(percentual);
        assertEquals(new BigDecimal("0.01500000"), rate.decimalValue());
    }

    @Test
    @DisplayName("Deve criar InterestRate com taxa zero")
    void deveCriarInterestRateComTaxaZero() {
        InterestRate rate = InterestRate.fromPercentual(BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00000000"), rate.decimalValue());
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar criar InterestRate com percentual negativo")
    void deveLancarExcecaoComPercentualNegativo() {
        BigDecimal percentualNegativo = new BigDecimal("-0.5");
        DomainValidationException exception = assertThrows(DomainValidationException.class, () -> {
            InterestRate.fromPercentual(percentualNegativo);
        });
        assertEquals("A taxa de juros não pode ser negativa.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar criar InterestRate com percentual nulo")
    void deveLancarExcecaoComPercentualNulo() {
        DomainValidationException exception = assertThrows(DomainValidationException.class, () -> {
            InterestRate.fromPercentual(null);
        });
        assertEquals("A taxa de juros não pode ser negativa.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve aplicar arredondamento HALF_EVEN na conversao de taxa")
    void deveAplicarArredondamentoHalfEvenNaConversao() {
        BigDecimal complexo = new BigDecimal("1.555555555");
        InterestRate rate = InterestRate.fromPercentual(complexo);
        assertEquals(new BigDecimal("0.01555556"), rate.decimalValue());
    }
}
