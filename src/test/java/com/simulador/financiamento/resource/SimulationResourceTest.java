package com.simulador.financiamento.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class SimulationResourceTest {

    @Test
    @DisplayName("Deve criar uma simulação financeira válida com sucesso e retornar status 201 Created")
    void deveCriarSimulacaoComSucesso() {
        String payload = """
            {
                "principal": 50000.00,
                "interestRatePercent": 1.75,
                "durationMonths": 24
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/simulacoes")
        .then()
            .statusCode(201)
            .header("Location", containsString("/simulacoes/"))
            .body("id", notNullValue())
            .body("principal", equalTo(50000.0f))
            .body("interestRate", equalTo(0.01750000f))
            .body("durationMonths", equalTo(24))
            .body("finalBalance", notNullValue())
            .body("totalInterest", notNullValue())
            .body("steps", hasSize(24))
            .body("steps[0].month", equalTo(1))
            .body("steps[0].initialBalance", equalTo(50000.0f))
            .body("steps[23].month", equalTo(24));
    }

    @Test
    @DisplayName("Deve lançar status 400 Bad Request ao simular com parâmetros brutos inválidos")
    void deveLancarErro400AoCriarComDadosInvalidos() {
        String payload = """
            {
                "principal": -100.00,
                "interestRatePercent": 1.75,
                "durationMonths": 12
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/simulacoes")
        .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("error", equalTo("Bad Request"))
            .body("message", containsString("O valor monetário não pode ser negativo"))
            .body("path", equalTo("/simulacoes"))
            .body("timestamp", notNullValue());
    }

    @Test
    @DisplayName("Deve buscar por ID uma simulação criada anteriormente com sucesso")
    void deveBuscarSimulacaoPorIdComSucesso() {
        String payload = """
            {
                "principal": 15000.00,
                "interestRatePercent": 2.2,
                "durationMonths": 12
            }
            """;

        // Cria a simulação e captura o ID do JSON retornado
        String id = given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/simulacoes")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Executa a busca pelo ID gerado
        given()
        .when()
            .get("/simulacoes/" + id)
        .then()
            .statusCode(200)
            .body("id", equalTo(id))
            .body("principal", equalTo(15000.0f))
            .body("interestRate", equalTo(0.02200000f))
            .body("durationMonths", equalTo(12))
            .body("steps", hasSize(12));
    }

    @Test
    @DisplayName("Deve retornar status 404 Not Found com corpo de erro padronizado ao buscar ID inexistente")
    void deveRetornar404AoBuscarInexistente() {
        String nonExistentId = UUID.randomUUID().toString();

        given()
        .when()
            .get("/simulacoes/" + nonExistentId)
        .then()
            .statusCode(404)
            .body("status", equalTo(404))
            .body("error", equalTo("Not Found"))
            .body("message", containsString("Simulação não encontrada com o ID: " + nonExistentId))
            .body("path", equalTo("/simulacoes/" + nonExistentId))
            .body("timestamp", notNullValue());
    }
}
