package com.simulador.financiamento.resource;

import com.simulador.financiamento.repository.SimulationEntity;
import com.simulador.financiamento.resource.dto.ErrorResponse;
import com.simulador.financiamento.resource.dto.SimulationRequest;
import com.simulador.financiamento.resource.dto.SimulationResponse;
import com.simulador.financiamento.service.SimulationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.net.URI;

/**
 * Controlador REST que expõe os endpoints HTTP/JSON para simulação de financiamentos
 * na rota "/simulacoes". Lida com conversão de DTOs, cabeçalhos de resposta HTTP 
 * e delegação de lógica para a camada de Serviço.
 */
@Path("/simulacoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(
    info = @Info(
        title = "API de Simulação de Financiamentos e Investimentos",
        version = "1.0.0",
        description = "API para simulação de produtos financeiros de crédito e investimentos, "
            + "focada em precisão matemática extrema (BACEn), código limpo sob DDD Pragmático e alta manutenibilidade.",
        contact = @Contact(
            name = "Engenharia de Software Financeira",
            email = "desenvolvimento@simulador-api.com"
        )
    )
)
@Tag(name = "Simulações Financeiras", description = "Endpoints para criação e recuperação de simulações com evolução mensal de juros compostos")
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
    @Operation(
        summary = "Submete uma nova simulação financeira de crédito",
        description = "Executa de forma Fail-Fast a simulação com juros compostos. Valida as regras de negócio "
            + "(principal positivo, prazo maior que zero, taxa não negativa), gera a evolução evolutiva mensal "
            + "(SimulationStep) de juros e persiste o agregado na base de dados relacional H2."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Simulação calculada e gravada no banco com absoluto sucesso.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SimulationResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Parâmetros de entrada inválidos (violação das invariantes de domínio ou JSON malformado).",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public Response criarSimulacao(SimulationRequest request) {
        if (request == null) {
            throw new BadRequestException("O corpo da requisição não pode ser vazio.");
        }

        // Delega para o serviço, que executa validações de domínio Fail-Fast e persistência
        SimulationEntity entity = service.simulateAndSave(
            request.valorInicial(),
            request.taxaJurosMensal(),
            request.prazoMeses(),
            request.tipoJuros()
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
    @Operation(
        summary = "Recupera uma simulação consolidada pelo ID exclusivo",
        description = "Busca no banco de dados H2 a simulação cadastrada sob o identificador UUID fornecido. "
            + "Reconstrói o agregado relacional completo junto com sua memória de cálculo mensal associada."
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Simulação financeira localizada e retornada.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SimulationResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Nenhum registro de simulação localizado para o UUID fornecido.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public Response buscarPorId(
        @Parameter(
            description = "Código exclusivo de identificação da simulação (UUID formato String)",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @PathParam("id") String id
    ) {
        // Busca a entidade de persistência (disparará exceção de negócio 404 se não existir)
        SimulationEntity entity = service.findById(id);

        // Mapeia para o DTO imutável
        SimulationResponse responseDto = SimulationResponse.fromEntity(entity);

        return Response.ok(responseDto)
            .build();
    }
}

