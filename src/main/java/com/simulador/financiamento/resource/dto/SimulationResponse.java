package com.simulador.financiamento.resource.dto;

import com.simulador.financiamento.repository.SimulationEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Record DTO imutável contendo os dados de retorno consolidados e a memória de cálculo evolutiva 
 * completa de um financiamento simulado.
 * 
 * @param id O identificador exclusivo UUID da simulação gravada.
 * @param principal O valor de principal solicitado originalmente.
 * @param interestRate A taxa de juros decimal aplicada mensalmente.
 * @param durationMonths O prazo de vigência em meses.
 * @param finalBalance O saldo devedor consolidado ao término do prazo.
 * @param totalInterest O somatório consolidado de todos os juros incidentes acumulados.
 * @param steps Lista contendo os passos mensais detalhados (memória de cálculo).
 */
public record SimulationResponse(
    String id,
    BigDecimal principal,
    BigDecimal interestRate,
    int durationMonths,
    BigDecimal finalBalance,
    BigDecimal totalInterest,
    List<SimulationStepResponse> steps
) {
    /**
     * Mapeia uma entidade persistente {@link SimulationEntity} para a representação DTO imutável correspondente.
     * 
     * @param entity A entidade a ser convertida.
     * @return O DTO {@link SimulationResponse} populado com os dados e a memória de cálculo.
     */
    public static SimulationResponse fromEntity(SimulationEntity entity) {
        if (entity == null) {
            return null;
        }
        
        List<SimulationStepResponse> stepDtos = entity.getSteps().stream()
            .map(SimulationStepResponse::fromEntity)
            .collect(Collectors.toList());

        return new SimulationResponse(
            entity.getId(),
            entity.getPrincipalAmount(),
            entity.getInterestRate(),
            entity.getDurationMonths(),
            entity.getFinalBalance(),
            entity.getTotalInterest(),
            stepDtos
        );
    }
}
