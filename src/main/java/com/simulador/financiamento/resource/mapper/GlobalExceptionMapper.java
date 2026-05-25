package com.simulador.financiamento.resource.mapper;

import com.simulador.financiamento.resource.dto.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * Provedor de exceções global (ExceptionMapper) encarregado de interceptar qualquer
 * exceção sistêmica ou inesperada (Throwable) que não foi devidamente tratada nas
 * camadas inferiores.
 * 
 * Evita o vazamento de stack traces internos do banco H2, infraestrutura ou do servidor
 * para o cliente final, retornando uma resposta limpa e padronizada no formato {@link ErrorResponse}.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    /**
     * Intercepta a exceção e monta o objeto {@link ErrorResponse} correspondente com status HTTP 500.
     * Preserva o comportamento de exceções nativas do JAX-RS (como 404, 405) repassando seus códigos.
     * 
     * @param exception A exceção capturada.
     * @return Resposta HTTP JSON padronizada.
     */
    @Override
    public Response toResponse(Throwable exception) {
        // Se já for uma exceção web padrão do JAX-RS/Quarkus (ex: BadRequestException 400, NotFoundException 404),
        // preservamos o código de status e a mensagem original.
        if (exception instanceof WebApplicationException webAppException) {
            Response originalResponse = webAppException.getResponse();
            int status = originalResponse.getStatus();
            String message = exception.getMessage();

            // Evita expor mensagens em branco
            if (message == null || message.trim().isEmpty()) {
                message = originalResponse.getStatusInfo().getReasonPhrase();
            }

            ErrorResponse errorDto = new ErrorResponse(
                status,
                originalResponse.getStatusInfo().getReasonPhrase(),
                message,
                uriInfo.getPath()
            );

            return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorDto)
                .build();
        }

        // Para falhas sistêmicas gerais (ConnectionException, NullPointerException, etc.),
        // registramos a falha com o stack trace completo nos logs protegidos do servidor.
        LOG.error("Erro inesperado processado pelo GlobalExceptionMapper", exception);

        // Devolvemos status 500 genérico de forma limpa e segura ao cliente final
        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        ErrorResponse errorDto = new ErrorResponse(
            status,
            "Internal Server Error",
            "Ocorreu um erro interno e inesperado no servidor. Por favor, tente novamente mais tarde ou contate o suporte.",
            uriInfo.getPath()
        );

        return Response.status(status)
            .type(MediaType.APPLICATION_JSON)
            .entity(errorDto)
            .build();
    }
}
