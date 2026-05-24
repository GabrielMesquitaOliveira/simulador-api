package com.simulador.financiamento.resource.dto;

import com.simulador.financiamento.repository.SimulationStepEntity;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Record DTO imutável que representa uma etapa individual (mês) de evolução do saldo devedor
 * na memória de cálculo retornada na resposta da simulação em conformidade com o edital do hackathon.
 */
@Schema(name = "SimulationStepResponse", description = "Etapa individual (mês) detalhando a evolução do financiamento")
public record SimulationStepResponse(
    @Schema(description = "Número do mês correspondente ao passo da evolução", examples = {"1"}, required = true)
    int mes,

    @Schema(description = "Saldo devedor no início deste período antes do acréscimo de juros", examples = {"1000.00"}, required = true)
    BigDecimal saldoInicial,

    @Schema(description = "Montante de juros calculado para o respectivo período", examples = {"15.00"}, required = true)
    BigDecimal juro,

    @Schema(description = "Saldo devedor resultante ao final deste período (saldo inicial + juro)", examples = {"1015.00"}, required = true)
    BigDecimal saldoFinal
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
