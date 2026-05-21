package com.simulador.financiamento.repository;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidade de persistência JPA que representa uma etapa individual (linha de memória de cálculo)
 * associada a uma simulação de financiamento. Mapeada para a tabela "simulation_step".
 */
@Entity
@Table(name = "simulation_step")
public class SimulationStepEntity {

    /**
     * Identificador exclusivo da etapa (UUID gerado pela aplicação).
     */
    @Id
    private String id;

    /**
     * Referência bidirecional para a simulação pai à qual este passo pertence.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id", nullable = false)
    private SimulationEntity simulation;

    /**
     * O número ordinal do mês da simulação (1-indexed).
     */
    @Column(name = "month_number", nullable = false)
    private int monthNumber;

    /**
     * O saldo devedor inicial no começo deste mês específico.
     */
    @Column(name = "initial_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal initialBalance;

    /**
     * O montante de juros calculado e incidido durante este mês.
     */
    @Column(name = "interest_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal interestAmount;

    /**
     * O saldo devedor final ao término deste mês.
     */
    @Column(name = "final_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalBalance;

    /**
     * Construtor padrão exigido pela especificação JPA.
     */
    public SimulationStepEntity() {}

    // Getters e Setters com JavaDocs em português

    /**
     * Obtém o ID exclusivo da etapa.
     * 
     * @return O identificador exclusivo.
     */
    public String getId() {
        return id;
    }

    /**
     * Define o ID exclusivo da etapa.
     * 
     * @param id O identificador a ser definido.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Obtém a simulação pai associada a esta etapa.
     * 
     * @return A entidade {@link SimulationEntity} pai.
     */
    public SimulationEntity getSimulation() {
        return simulation;
    }

    /**
     * Define a simulação pai associada a esta etapa.
     * 
     * @param simulation A simulação a ser definida.
     */
    public void setSimulation(SimulationEntity simulation) {
        this.simulation = simulation;
    }

    /**
     * Obtém o número ordinal do mês da etapa.
     * 
     * @return O número do mês.
     */
    public int getMonthNumber() {
        return monthNumber;
    }

    /**
     * Define o número ordinal do mês da etapa.
     * 
     * @param monthNumber O número do mês.
     */
    public void setMonthNumber(int monthNumber) {
        this.monthNumber = monthNumber;
    }

    /**
     * Obtém o saldo inicial da etapa.
     * 
     * @return O saldo inicial.
     */
    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    /**
     * Define o saldo inicial da etapa.
     * 
     * @param initialBalance O saldo inicial.
     */
    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    /**
     * Obtém o valor dos juros da etapa.
     * 
     * @return O valor dos juros.
     */
    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    /**
     * Define o valor dos juros da etapa.
     * 
     * @param interestAmount O valor dos juros.
     */
    public void setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
    }

    /**
     * Obtém o saldo final da etapa.
     * 
     * @return O saldo final.
     */
    public BigDecimal getFinalBalance() {
        return finalBalance;
    }

    /**
     * Define o saldo final da etapa.
     * 
     * @param finalBalance O saldo final.
     */
    public void setFinalBalance(BigDecimal finalBalance) {
        this.finalBalance = finalBalance;
    }
}
