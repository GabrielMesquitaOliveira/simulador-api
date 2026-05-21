package com.simulador.financiamento.resource.dto;

import com.simulador.financiamento.repository.SimulationStepEntity;
import java.math.BigDecimal;

/**
 * Record DTO imutável que representa uma etapa individual (mês) de evolução do saldo devedor
 * na memória de cálculo retornada na resposta da simulação.
 * 
 * @param month O número ordinal do mês da evolução (1-indexed).
 * @param initialBalance O saldo devedor no início deste mês.
 * @param interestAmount O valor de juros calculado e acrescido neste mês.
 * @param finalBalance O saldo devedor acumulado ao término deste mês.
 */
public record SimulationStepResponse(
    int month,
    BigDecimal initialBalance,
    BigDecimal interestAmount,
    BigDecimal finalBalance
) {
    /**
     * Mapeia uma entidade persistente {@link SimulationStepEntity} para a representação DTO imutável correspondente.
     * 
     * @param entity A entidade a ser convertida.
     * @return O DTO {@link SimulationStepResponse} populado com os dados relacionais.
     */
    public static SimulationStepResponse fromEntity(SimulationStepEntity entity) {
        if (entity == null) {
            return null;
        }
        return new SimulationStepResponse(
            entity.getMonthNumber(),
            entity.getInitialBalance(),
            entity.getInterestAmount(),
            entity.getFinalBalance()
        );
    }
}
