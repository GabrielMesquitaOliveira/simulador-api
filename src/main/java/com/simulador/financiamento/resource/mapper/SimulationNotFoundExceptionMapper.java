package com.simulador.financiamento.resource.mapper;

import com.simulador.financiamento.service.SimulationNotFoundException;
import com.simulador.financiamento.resource.dto.ErrorResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Provedor JAX-RS (ExceptionMapper) encarregado de interceptar as exceções do tipo 
 * {@link SimulationNotFoundException} (erros de consultas falhas na persistência) 
 * e convertê-las em respostas HTTP padronizadas com status 404 (Not Found).
 */
@Provider
public class SimulationNotFoundExceptionMapper implements ExceptionMapper<SimulationNotFoundException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(SimulationNotFoundException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
            Response.Status.NOT_FOUND.getStatusCode(),
            "Not Found",
            exception.getMessage(),
            uriInfo.getPath()
        );

        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(errorResponse)
            .build();
    }
}
