package com.simulador.financiamento.resource;

import com.simulador.financiamento.repository.SimulationEntity;
import com.simulador.financiamento.resource.dto.SimulationRequest;
import com.simulador.financiamento.resource.dto.SimulationResponse;
import com.simulador.financiamento.service.SimulationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;

/**
 * Controlador REST que expõe os endpoints HTTP/JSON para simulação de financiamentos
 * na rota "/simulacoes". Lida com conversão de DTOs, cabeçalhos de resposta HTTP 
 * e delegação de lógica para a camada de Serviço.
 */
@Path("/simulacoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SimulationResource {

    @Inject
    SimulationService service;

    /**
     * Endpoint POST para submissão de uma nova simulação financeira.
     * 
     * @param request O DTO imutável contendo os valores solicitados.
     * @return Resposta HTTP 201 (Created) com a memória de cálculo gerada e cabeçalho "Location".
     */
    @POST
    public Response criarSimulacao(SimulationRequest request) {
        if (request == null) {
            throw new BadRequestException("O corpo da requisição não pode ser vazio.");
        }

        // Delega para o serviço, que executa validações de domínio Fail-Fast e persistência
        SimulationEntity entity = service.simulateAndSave(
            request.principal(),
            request.interestRatePercent(),
            request.durationMonths()
        );

        // Mapeia a entidade JPA mutável para o DTO imutável SimulationResponse
        SimulationResponse responseDto = SimulationResponse.fromEntity(entity);

        // Gera a URI de localização do novo recurso
        URI locationUri = URI.create("/simulacoes/" + entity.getId());

        return Response.created(locationUri)
            .entity(responseDto)
            .build();
    }

    /**
     * Endpoint GET para recuperar uma simulação financeira gravada anteriormente.
     * 
     * @param id O UUID da simulação solicitado.
     * @return Resposta HTTP 200 (OK) com os dados consolidados e a evolução mensal.
     */
    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") String id) {
        // Busca a entidade de persistência (disparará exceção de negócio 404 se não existir)
        SimulationEntity entity = service.findById(id);

        // Mapeia para o DTO imutável
        SimulationResponse responseDto = SimulationResponse.fromEntity(entity);

        return Response.ok(responseDto)
            .build();
    }
}
