package com.simulador.financiamento.resource.dto;

import java.math.BigDecimal;

/**
 * Record DTO imutável que representa os dados de entrada necessários para a criação 
 * de uma simulação de financiamento.
 * 
 * @param principal O valor do saldo principal solicitado para o financiamento.
 * @param interestRatePercent A taxa de juros nominal percentual ao mês (ex: 1.5 para 1.5%).
 * @param durationMonths O prazo total do financiamento expresso em meses.
 */
public record SimulationRequest(
    BigDecimal principal,
    BigDecimal interestRatePercent,
    int durationMonths
) {}
