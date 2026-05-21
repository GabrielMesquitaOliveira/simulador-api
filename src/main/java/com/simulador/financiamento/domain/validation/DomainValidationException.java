package com.simulador.financiamento.domain.validation;

/**
 * Exceção de domínio lançada sempre que uma invariante ou regra de negócio
 * for violada. Funciona como o mecanismo Fail-Fast do domínio para garantir
 * a consistência do estado das entidades e Value Objects.
 */
public class DomainValidationException extends RuntimeException {
    
    /**
     * Constrói uma nova exceção com a mensagem detalhada da violação.
     * 
     * @param message A mensagem explicativa da violação de negócio.
     */
    public DomainValidationException(String message) {
        super(message);
    }
}
