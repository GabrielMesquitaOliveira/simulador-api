package com.simulador.financiamento.resource.dto;

import java.time.LocalDateTime;

/**
 * Record DTO imutável que padroniza o formato de resposta de erros HTTP da API.
 * 
 * @param timestamp O carimbo de data/hora em que a falha ocorreu.
 * @param status O código de status HTTP correspondente.
 * @param error O título legível ou categoria do erro.
 * @param message O detalhamento descritivo sobre o motivo do erro.
 * @param path A URI do recurso solicitada que gerou a falha.
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {
    /**
     * Construtor de conveniência que define automaticamente o timestamp para o momento atual.
     * 
     * @param status O código do status HTTP.
     * @param error O título do erro.
     * @param message A mensagem descritiva.
     * @param path O endpoint solicitado.
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path);
    }
}
