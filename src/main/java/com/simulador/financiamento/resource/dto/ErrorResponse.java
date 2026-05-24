package com.simulador.financiamento.resource.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Record DTO imutável que padroniza o formato de resposta de erros HTTP da API.
 */
@Schema(name = "ErrorResponse", description = "Estrutura padrão de retorno para mensagens de erro HTTP")
public record ErrorResponse(
    @Schema(description = "Carimbo de data e hora em que a falha foi registrada na API", examples = {"2026-05-24T12:00:00"}, required = true)
    LocalDateTime timestamp,

    @Schema(description = "Código de status HTTP associado à falha", examples = {"400"}, required = true)
    int status,

    @Schema(description = "Categoria descritiva padrão ou tipo simplificado do erro HTTP", examples = {"Bad Request"}, required = true)
    String error,

    @Schema(description = "Mensagem altamente descritiva sobre o motivo da falha ou regra violada", examples = {"O valor monetário não pode ser negativo."}, required = true)
    String message,

    @Schema(description = "URI relativa ou endpoint que originou a requisição com falha", examples = {"/simulacoes"}, required = true)
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
