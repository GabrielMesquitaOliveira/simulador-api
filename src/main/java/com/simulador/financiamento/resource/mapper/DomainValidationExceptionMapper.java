package com.simulador.financiamento.resource.mapper;

import com.simulador.financiamento.domain.validation.DomainValidationException;
import com.simulador.financiamento.resource.dto.ErrorResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Provedor JAX-RS (ExceptionMapper) encarregado de interceptar as exceções do tipo 
 * {@link DomainValidationException} (quebras de invariantes de domínio Fail-Fast) 
 * e convertê-las em respostas HTTP padronizadas com status 400 (Bad Request).
 */
@Provider
public class DomainValidationExceptionMapper implements ExceptionMapper<DomainValidationException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(DomainValidationException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
            Response.Status.BAD_REQUEST.getStatusCode(),
            "Bad Request",
            exception.getMessage(),
            uriInfo.getPath()
        );

        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(errorResponse)
            .build();
    }
}
