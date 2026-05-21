package com.simulador.financiamento.repository;

import com.simulador.financiamento.domain.shared.InterestRate;
import com.simulador.financiamento.domain.shared.Money;
import com.simulador.financiamento.domain.simulation.Simulation;
import com.simulador.financiamento.domain.simulation.SimulationStep;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Entidade de persistência JPA que atua como o mapeador relacional da raiz do agregado
 * de simulação (Aggregate Root). Mapeada para a tabela "simulation".
 */
@Entity
@Table(name = "simulation")
public class SimulationEntity {

    /**
     * Identificador exclusivo da simulação (UUID gerado pela aplicação ou banco).
     */
    @Id
    private String id;

    /**
     * O valor de principal simulado.
     */
    @Column(name = "principal_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal principalAmount;

    /**
     * A taxa de juros decimal aplicada (escala 8 para precisão BACEn).
     */
    @Column(name = "interest_rate", nullable = false, precision = 18, scale = 8)
    private BigDecimal interestRate;

    /**
     * A duração do prazo de simulação em meses.
     */
    @Column(name = "duration_months", nullable = false)
    private int durationMonths;

    /**
     * Saldo final devedor consolidado ao término do prazo.
     */
    @Column(name = "final_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalBalance;

    /**
     * Acumulado consolidado dos juros pagos na simulação.
     */
    @Column(name = "total_interest", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalInterest;

    /**
     * Timestamp de criação do registro de simulação.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Lista de passos e evolução mensal associados a esta simulação.
     * Mapeada com cascateamento total (CascadeType.ALL e orphanRemoval = true)
     * para garantir a consistência íntegra do Agregado.
     */
    @OneToMany(mappedBy = "simulation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SimulationStepEntity> steps = new ArrayList<>();

    /**
     * Construtor padrão obrigatório para especificações JPA.
     */
    public SimulationEntity() {}

    /**
     * Traduz e mapeia esta entidade persistente JPA de volta para o modelo de domínio puro e imutável.
     * 
     * @return Um {@link Simulation} imutável com todas as suas invariantes íntegras.
     */
    public Simulation toDomain() {
        List<SimulationStep> domainSteps = this.steps.stream()
            .map(s -> new SimulationStep(
                s.getMonthNumber(),
                new Money(s.getInitialBalance()),
                new Money(s.getInterestAmount()),
                new Money(s.getFinalBalance())
            ))
            .collect(Collectors.toList());

        return new Simulation(
            new Money(this.principalAmount),
            new InterestRate(this.interestRate),
            this.durationMonths,
            new Money(this.finalBalance),
            new Money(this.totalInterest),
            domainSteps
        );
    }

    /**
     * Mapeia um objeto de domínio puro para uma nova estrutura de entidade JPA.
     * Gera automaticamente IDs (UUIDs) para todos os passos filhos associados.
     * 
     * @param domain O modelo puro de domínio a ser mapeado.
     * @return Uma nova instância de {@link SimulationEntity} pronta para persistência.
     */
    public static SimulationEntity fromDomain(Simulation domain) {
        SimulationEntity entity = new SimulationEntity();
        entity.setPrincipalAmount(domain.principal().amount());
        entity.setInterestRate(domain.rate().decimalValue());
        entity.setDurationMonths(domain.durationMonths());
        entity.setFinalBalance(domain.finalBalance().amount());
        entity.setTotalInterest(domain.totalInterest().amount());

        List<SimulationStepEntity> stepEntities = domain.steps().stream()
            .map(s -> {
                SimulationStepEntity stepEntity = new SimulationStepEntity();
                stepEntity.setId(UUID.randomUUID().toString());
                stepEntity.setSimulation(entity);
                stepEntity.setMonthNumber(s.month());
                stepEntity.setInitialBalance(s.initialBalance().amount());
                stepEntity.setInterestAmount(s.interestAmount().amount());
                stepEntity.setFinalBalance(s.finalBalance().amount());
                return stepEntity;
            })
            .collect(Collectors.toList());

        entity.setSteps(stepEntities);
        return entity;
    }

    // Getters e Setters com JavaDocs em português

    /**
     * Obtém o ID da simulação.
     * 
     * @return O ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Define o ID da simulação.
     * 
     * @param id O ID a ser definido.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Obtém o montante principal.
     * 
     * @return O principal.
     */
    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    /**
     * Define o montante principal.
     * 
     * @param principalAmount O principal.
     */
    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    /**
     * Obtém a taxa de juros decimal.
     * 
     * @return A taxa.
     */
    public BigDecimal getInterestRate() {
        return interestRate;
    }

    /**
     * Define a taxa de juros decimal.
     * 
     * @param interestRate A taxa.
     */
    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    /**
     * Obtém a duração do financiamento.
     * 
     * @return O prazo em meses.
     */
    public int getDurationMonths() {
        return durationMonths;
    }

    /**
     * Define a duração do financiamento.
     * 
     * @param durationMonths O prazo em meses.
     */
    public void setDurationMonths(int durationMonths) {
        this.durationMonths = durationMonths;
    }

    /**
     * Obtém o saldo final acumulado.
     * 
     * @return O saldo final.
     */
    public BigDecimal getFinalBalance() {
        return finalBalance;
    }

    /**
     * Define o saldo final acumulado.
     * 
     * @param finalBalance O saldo final.
     */
    public void setFinalBalance(BigDecimal finalBalance) {
        this.finalBalance = finalBalance;
    }

    /**
     * Obtém o total dos juros calculados.
     * 
     * @return O total de juros.
     */
    public BigDecimal getTotalInterest() {
        return totalInterest;
    }

    /**
     * Define o total dos juros calculados.
     * 
     * @param totalInterest O total de juros.
     */
    public void setTotalInterest(BigDecimal totalInterest) {
        this.totalInterest = totalInterest;
    }

    /**
     * Obtém o timestamp de criação da simulação.
     * 
     * @return A data/hora de criação.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Define o timestamp de criação.
     * 
     * @param createdAt A data/hora de criação.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Obtém a lista de entidades de passos da simulação.
     * 
     * @return A lista de passos.
     */
    public List<SimulationStepEntity> getSteps() {
        return steps;
    }

    /**
     * Define a lista de entidades de passos da simulação.
     * 
     * @param steps A lista de passos a ser definida.
     */
    public void setSteps(List<SimulationStepEntity> steps) {
        this.steps = steps;
    }
}
