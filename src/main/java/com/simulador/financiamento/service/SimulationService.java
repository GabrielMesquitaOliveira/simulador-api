package com.simulador.financiamento.service;

import com.simulador.financiamento.domain.shared.InterestRate;
import com.simulador.financiamento.domain.shared.Money;
import com.simulador.financiamento.domain.simulation.CompoundInterestStrategy;
import com.simulador.financiamento.domain.simulation.Simulation;
import com.simulador.financiamento.domain.validation.DomainValidationException;
import com.simulador.financiamento.repository.SimulationEntity;
import com.simulador.financiamento.repository.SimulationRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço de aplicação encarregado de orquestrar os casos de uso de simulação
 * de financiamentos. Gerencia transações e atua como ponte entre o domínio puro
 * e a infraestrutura de persistência.
 */
@ApplicationScoped
public class SimulationService {

    @Inject
    SimulationRepository repository;

    @Inject
    MeterRegistry registry;

    // Controladores thread-safe para atualizar o Gauge de média de taxas simuladas em tempo real
    private final AtomicLong totalSimulations = new AtomicLong(0);
    private final AtomicLong sumInterestRatesBits = new AtomicLong(Double.doubleToRawLongBits(0.0));

    /**
     * Inicializa a telemetria do microsserviço registrando o Gauge de média de taxas
     * e populando seu estado inicial com base nas simulações já persistidas no banco.
     */
    @PostConstruct
    void init() {
        // Registra o Gauge na inicialização do Bean CDI
        registry.gauge("simulation_average_interest_rate_percent", this, service -> {
            long count = service.totalSimulations.get();
            if (count == 0) {
                return 0.0;
            }
            double sum = Double.longBitsToDouble(service.sumInterestRatesBits.get());
            return sum / count;
        });

        // Tenta buscar o histórico gravado de simulações para carregar a média inicial
        try {
            long count = repository.count();
            if (count > 0) {
                BigDecimal avgRateDecimal = repository.find("select avg(s.interestRate) from SimulationEntity s")
                    .project(BigDecimal.class)
                    .firstResult();
                
                if (avgRateDecimal != null) {
                    totalSimulations.set(count);
                    // Como a taxa está no banco em base decimal (ex: 0.0150 para 1.5%), multiplicamos por 100
                    double avgRatePercent = avgRateDecimal.multiply(BigDecimal.valueOf(100)).doubleValue();
                    sumInterestRatesBits.set(Double.doubleToRawLongBits(avgRatePercent * count));
                }
            }
        } catch (Exception e) {
            // Em testes unitários leves que mockam o repositório, toleramos falhas na leitura inicial
        }
    }

    /**
     * Orquestra a simulação de um financiamento utilizando juros compostos e
     * persiste o agregado correspondente no banco de dados.
     * 
     * Aplica políticas de Fault Tolerance para tratamento de concorrência concorrente 
     * H2 (@Retry) e limite de duração do processo (@Timeout).
     * 
     * @param principal O valor de principal solicitado.
     * @param interestRatePercent A taxa de juros percentual ao mês (ex: 1.5 para 1.5%).
     * @param durationMonths A duração total do financiamento em meses.
     * @return A entidade persistida {@link SimulationEntity} contendo os dados e a memória de cálculo.
     */
    @Transactional
    @Retry(maxRetries = 3, delay = 100, delayUnit = ChronoUnit.MILLIS, abortOn = DomainValidationException.class)
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    public SimulationEntity simulateAndSave(BigDecimal principal, BigDecimal interestRatePercent, int durationMonths) {
        long startTime = System.nanoTime();
        try {
            // Converte e valida de forma Fail-Fast os parâmetros brutos em Value Objects de domínio
            Money moneyPrincipal = new Money(principal);
            InterestRate rate = InterestRate.fromPercentual(interestRatePercent);

            // Executa o cálculo financeiro no agregado de domínio rico
            Simulation domainSimulation = Simulation.execute(
                moneyPrincipal, 
                rate, 
                durationMonths, 
                new CompoundInterestStrategy()
            );

            // Converte o agregado de domínio para a entidade de persistência JPA
            SimulationEntity entity = SimulationEntity.fromDomain(domainSimulation);
            
            // Gera e atribui o identificador exclusivo (UUID) para a simulação pai
            String generatedId = UUID.randomUUID().toString();
            entity.setId(generatedId);

            // Persiste o agregado relacional completo (com passos em cascata) no banco H2
            repository.persist(entity);

            // Atualiza os contadores em memória do Gauge (Double bits thread-safe)
            totalSimulations.incrementAndGet();
            sumInterestRatesBits.updateAndGet(currentBits -> {
                double currentValue = Double.longBitsToDouble(currentBits);
                return Double.doubleToRawLongBits(currentValue + interestRatePercent.doubleValue());
            });

            // Registra as métricas de sucesso customizadas no Micrometer
            registry.counter("simulations_requested_total", "status", "success").increment();
            registry.summary("simulation_principal_brl").record(principal.doubleValue());
            registry.summary("simulation_duration_months").record(durationMonths);

            return entity;
        } catch (DomainValidationException e) {
            // Conta métrica de violação de regra de negócio (sem retentar no @Retry devido ao abortOn)
            registry.counter("simulations_requested_total", "status", "validation_failed").increment();
            throw e;
        } catch (Throwable t) {
            // Conta falhas de sistema inesperadas (banco fora do ar, estouro de conexões, etc.)
            registry.counter("simulations_requested_total", "status", "error").increment();
            throw t;
        } finally {
            // Registra o tempo gasto na simulação via Timer
            long duration = System.nanoTime() - startTime;
            registry.timer("simulation_calculation_duration_seconds")
                .record(duration, TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Busca uma simulação persistida pelo seu identificador exclusivo.
     * 
     * @param id O UUID da simulação em formato String.
     * @return A entidade persistida {@link SimulationEntity} localizada.
     * @throws SimulationNotFoundException se nenhuma simulação for localizada com o ID fornecido.
     */
    public SimulationEntity findById(String id) {
        SimulationEntity entity = repository.findById(id);
        if (entity == null) {
            throw new SimulationNotFoundException("Simulação não encontrada com o ID: " + id);
        }
        return entity;
    }
}

