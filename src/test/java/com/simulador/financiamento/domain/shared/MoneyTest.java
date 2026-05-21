package com.simulador.financiamento.domain.shared;

import com.simulador.financiamento.domain.validation.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    @DisplayName("Deve criar Money com valor valido positivo")
    void deveCriarMoneyComValorValidoPositivo() {
        BigDecimal valor = new BigDecimal("1500.50");
        Money money = new Money(valor);
        assertEquals(new BigDecimal("1500.50"), money.amount());
    }

    @Test
    @DisplayName("Deve criar Money com valor zero")
    void deveCriarMoneyComValorZero() {
        Money money = new Money(BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), money.amount());
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar criar Money com valor negativo")
    void deveLancarExcecaoComValorNegativo() {
        BigDecimal valorNegativo = new BigDecimal("-10.00");
        DomainValidationException exception = assertThrows(DomainValidationException.class, () -> {
            new Money(valorNegativo);
        });
        assertEquals("O valor monetário não pode ser negativo.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar criar Money com valor nulo")
    void deveLancarExcecaoComValorNulo() {
        DomainValidationException exception = assertThrows(DomainValidationException.class, () -> {
            new Money(null);
        });
        assertEquals("O valor monetário não pode ser negativo.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve somar dois valores monetarios corretamente")
    void deveSomarDoisValoresMonetarios() {
        Money m1 = new Money(new BigDecimal("100.50"));
        Money m2 = new Money(new BigDecimal("50.25"));
        Money resultado = m1.add(m2);
        assertEquals(new BigDecimal("150.75"), resultado.amount());
    }

    @Test
    @DisplayName("Deve multiplicar valor monetario por um fator BigDecimal aplicando HALF_EVEN")
    void deveMultiplicarEArredondarComHalfEven() {
        Money m = new Money(new BigDecimal("100.00"));
        BigDecimal fator = new BigDecimal("0.0155");
        Money resultado = m.multiply(fator);
        assertEquals(new BigDecimal("1.55"), resultado.amount());

        Money m2 = new Money(new BigDecimal("100.15"));
        BigDecimal fator2 = new BigDecimal("0.15");
        assertEquals(new BigDecimal("15.02"), m2.multiply(fator2).amount());

        BigDecimal fator3 = new BigDecimal("0.25");
        assertEquals(new BigDecimal("25.04"), m2.multiply(fator3).amount());
    }

    @Test
    @DisplayName("Deve comparar valores monetarios corretamente")
    void deveCompararValoresMonetarios() {
        Money m1 = new Money(new BigDecimal("100.00"));
        Money m2 = new Money(new BigDecimal("200.00"));
        
        assertTrue(m1.isLessThan(m2));
        assertTrue(m2.isGreaterThan(m1));
        assertFalse(m1.isEqualTo(m2));
        assertTrue(m1.isEqualTo(new Money(new BigDecimal("100.00"))));
    }
}
