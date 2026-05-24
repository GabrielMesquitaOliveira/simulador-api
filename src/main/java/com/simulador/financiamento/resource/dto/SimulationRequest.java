package com.simulador.financiamento.resource.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Record DTO imutável que representa os dados de entrada necessários para a criação 
 * de uma simulação de financiamento em conformidade estrita com o edital do hackathon.
 */
@Schema(name = "SimulationRequest", description = "Estrutura com dados de entrada necessários para simular um financiamento")
public record SimulationRequest(
    @Schema(description = "Valor inicial (principal) solicitado para o financiamento", examples = {"1000.00"}, required = true)
    BigDecimal valorInicial,

    @Schema(description = "Taxa de juros nominal percentual ao mês (ex: 1.5 para 1,5% ao mês)", examples = {"1.5"}, required = true)
    BigDecimal taxaJurosMensal,

    @Schema(description = "Prazo total do financiamento expresso em meses", examples = {"12"}, required = true)
    int prazoMeses
) {}
