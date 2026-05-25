package com.simulador.financiamento.resource.health;

import com.simulador.financiamento.repository.SimulationRepository;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Health Check de Prontidão (Readiness Check) para verificar a disponibilidade de conexões
 * ativas e saudáveis com a base de dados relacional H2.
 * 
 * Usado pelo Kubernetes para determinar se o pod pode começar a receber tráfego HTTP.
 */
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    SimulationRepository repository;

    @Override
    public HealthCheckResponse call() {
        try {
            // Executa uma chamada leve de contagem de registros para provar conectividade
            repository.count();
            return HealthCheckResponse.named("H2 Database Connection Check")
                .up()
                .withData("database", "H2")
                .build();
        } catch (Exception e) {
            return HealthCheckResponse.named("H2 Database Connection Check")
                .down()
                .withData("error", e.getMessage())
                .build();
        }
    }
}
