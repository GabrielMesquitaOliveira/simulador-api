package com.simulador.financiamento.resource.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Health Check de Sobrevivência (Liveness Check) para verificar a saúde básica da JVM,
 * como limites de memória livre para prosseguir as execuções.
 * 
 * Usado pelo Kubernetes para reiniciar automaticamente o contêiner caso ele entre em estado inoperante.
 */
@Liveness
@ApplicationScoped
public class SystemHealthCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();

        // Se houver mais de 1MB de memória livre na JVM, consideramos a aplicação saudável
        boolean healthy = freeMemory > 1024 * 1024;

        return HealthCheckResponse.named("JVM Memory Check")
            .status(healthy)
            .withData("freeMemoryBytes", freeMemory)
            .withData("totalMemoryBytes", totalMemory)
            .withData("maxMemoryBytes", maxMemory)
            .build();
    }
}
