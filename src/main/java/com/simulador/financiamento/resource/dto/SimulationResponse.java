package com.simulador.financiamento.resource.dto;

import com.simulador.financiamento.repository.SimulationEntity;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Record DTO imutável contendo os dados de retorno consolidados e a memória de cálculo evolutiva 
 * completa de um financiamento simulado em conformidade estrita com o edital do hackathon.
 */
@Schema(name = "SimulationResponse", description = "Estrutura contendo o resumo consolidado e a memória de cálculo da simulação")
public record SimulationResponse(
    @Schema(description = "Identificador exclusivo gerado para a simulação (UUID)", examples = {"550e8400-e29b-41d4-a716-446655440000"}, required = true)
    String id,

    @Schema(description = "Valor inicial (principal) solicitado para o financiamento", examples = {"1000.00"}, required = true)
    BigDecimal valorInicial,

    @Schema(description = "Taxa de juros nominal percentual ao mês (ex: 1.5 para 1,5% ao mês)", examples = {"1.5"}, required = true)
    BigDecimal taxaJurosMensal,

    @Schema(description = "Prazo total de vigência do financiamento em meses", examples = {"12"}, required = true)
    int prazoMeses,

    @Schema(description = "Valor total final acumulado ao término do prazo", examples = {"1195.62"}, required = true)
    BigDecimal valorTotalFinal,

    @Schema(description = "Somatório de todos os juros acumulados e pagos no prazo", examples = {"195.62"}, required = true)
    BigDecimal valorTotalJuros,

    @Schema(description = "Lista contendo os passos mensais detalhados da evolução (Memória de Cálculo)", required = true)
    List<SimulationStepResponse> memoriaCalculo
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

        // A taxa de juros no banco de dados está guardada como valor decimal (ex: 0.01500000 para 1.5%).
        // Para retornar o parâmetro de entrada exatamente como enviado no edital, multiplicamos por 100
        BigDecimal taxaPercentual = entity.getInterestRate().multiply(BigDecimal.valueOf(100)).stripTrailingZeros();

        return new SimulationResponse(
            entity.getId(),
            entity.getPrincipalAmount(),
            taxaPercentual,
            entity.getDurationMonths(),
            entity.getFinalBalance(),
            entity.getTotalInterest(),
            stepDtos
        );
    }
}
