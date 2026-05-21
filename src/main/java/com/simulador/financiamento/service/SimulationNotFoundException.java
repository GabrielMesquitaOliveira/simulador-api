package com.simulador.financiamento.service;

/**
 * Exceção de negócio sinalizando que uma simulação solicitada não foi encontrada
 * na camada de persistência.
 */
public class SimulationNotFoundException extends RuntimeException {
    
    /**
     * Cria uma nova exceção com uma mensagem explicativa.
     * 
     * @param message A mensagem detalhando o identificador não encontrado.
     */
    public SimulationNotFoundException(String message) {
        super(message);
    }
}
