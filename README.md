# API de Simulação de Financiamentos e Investimentos

API de altíssima performance para simulação de produtos financeiros, construída sob os mais rigorosos padrões de engenharia de software contemporânea, utilizando **Java 25**, **Quarkus**, **Domain-Driven Design (DDD) Pragmático**, **TDD (Test-Driven Development)** e **Screaming Architecture**.

---

## 🏗️ A Arquitetura do Domínio (Screaming Architecture & DDD)

Seguindo o princípio da **Screaming Architecture (Arquitetura Grito)**, a estrutura de pastas do projeto deixa imediatamente clara a intenção de negócio do software. Nosso domínio é **100% puro e agnóstico de frameworks**. Não existem anotações como `@Entity`, `@Table`, `@Column` ou dependências do Quarkus/Hibernate no domínio. O coração financeiro da aplicação é blindado contra influências de infraestrutura.

O pacote base `com.simulador.financiamento.domain` é subdividido exclusivamente por interesses semânticos e fronteiras de agregados:

```mermaid
graph TD
    subgraph com.simulador.financiamento.domain [Pacote Base do Domínio]
        subgraph validation [validation - Assertion Concern]
            DomainValidator["DomainValidator (Validador Central)"]
            DomainValidationException["DomainValidationException (Exceção)"]
        end

        subgraph shared [shared - VOs Compartilhados]
            Money["Money (Value Object - Dinheiro)"]
            InterestRate["InterestRate (Value Object - Taxa de Juros)"]
        end

        subgraph simulation [simulation - Agregado Raiz da Simulação]
            Simulation["Simulation (Aggregate Root)"]
            SimulationStep["SimulationStep (Entidade Local)"]
            InterestCalculationStrategy["InterestCalculationStrategy (Interface)"]
            CompoundInterestStrategy["CompoundInterestStrategy (Fórmula Juros Compostos)"]
        end
    end

    shared --> validation
    simulation --> shared
    simulation --> validation
```

---

## 🎯 Padrões de Projeto e Regras de Negócio Implementados

Para facilitar a avaliação técnica da nossa arquitetura, detalhamos abaixo a responsabilidade de cada componente da camada de domínio:

### 1. Assertion Concern (Subpacote `domain.validation`)
Evitamos a pulverização de validações e condicionais `if` aninhadas nos construtores das entidades. Implementamos o padrão **Assertion Concern** por meio de:
* **`DomainValidator`**: Uma classe utilitária contendo asserções estáticas reutilizáveis (`requireNonNull`, `requireNonNegative`, `requirePositive`, `requireTrue`). Caso alguma invariante de negócio seja infringida, o domínio dispara imediatamente um comportamento **Fail-Fast**.
* **`DomainValidationException`**: Exceção de tempo de execução (`RuntimeException`) customizada que sinaliza quebras de integridade das regras do domínio.

### 2. Evitando Obsessão Primitiva (Subpacote `domain.shared`)
Representar valores monetários ou taxas de juros usando primitivos (`double`, `float`) ou diretamente `BigDecimal` sem semântica gera falhas de arredondamento e código frágil.
* **`Money` (Value Object)**: Record imutável que encapsula valores monetários. Garante que nenhuma quantia seja negativa, realiza operações aritméticas imutáveis (`add`, `multiply`) e força a precisão de **2 casas decimais** com arredondamento comercial **`RoundingMode.HALF_EVEN`** de forma transparente.
* **`InterestRate` (Value Object)**: Record imutável representativo da taxa de juros. Garante conformidade estrita com o **Banco Central do Brasil (BACEn)** ao trabalhar internamente com a escala de **8 casas decimais** e arredondamento **`RoundingMode.HALF_EVEN`**. Possui um método de fábrica (`fromPercentual`) que converte, por exemplo, `1.5` para `0.01500000` de forma segura.

### 3. Agregado de Simulação (Subpacote `domain.simulation`)
* **`Simulation` (Aggregate Root)**: A entidade raiz do agregado. É um record totalmente imutável que centraliza o estado consolidado da simulação (valor principal, taxa, prazo, saldo final acumulado, total de juros pagos e a memória de cálculo evolutiva). O construtor efetua uma **cópia defensiva imutável** da lista de parcelas para impedir modificações externas.
* **`SimulationStep` (Entidade Local)**: Representa uma linha detalhada da memória de cálculo evolutiva de determinado mês. Possui uma validação de **coerência matemática** que impede inconsistências: o construtor valida se o saldo devedor final do período é rigorosamente igual ao saldo inicial somado ao valor dos juros daquele mês (`finalBalance == initialBalance + interest`).
* **`InterestCalculationStrategy` (Strategy)**: Interface que define o contrato matemático para cálculo da evolução do financiamento.
* **`CompoundInterestStrategy` (Concrete Strategy)**: Implementação matemática do cálculo de juros compostos baseado na fórmula $M = C \times (1 + i)^n$, evoluindo e capitalizando o saldo mês a mês de forma imutável.

---

## 🔄 Fluxo de Execução da Simulação

O diagrama de sequência abaixo demonstra o fluxo de controle limpo quando uma nova simulação é disparada pelo domínio:

```mermaid
sequenceDiagram
    autonumber
    actor Client as Usuário/Serviço
    participant Sim as Simulation (Aggregate Root)
    participant Strat as CompoundInterestStrategy
    participant VO as Money / InterestRate
    participant Val as DomainValidator

    Client->>VO: Instancia Money(principal) e InterestRate(rate)
    activate VO
    VO->>Val: Valida não-nulidade e não-negatividade
    VO->>VO: Aplica arredondamento HALF_EVEN (escala 2 para Money e 8 para taxa)
    VO-->>Client: Instâncias válidas e imutáveis
    deactivate VO

    Client->>Sim: Simulation.execute(principal, rate, prazo, strategy)
    activate Sim
    Sim->>Strat: strategy.calculate(principal, rate, prazo)
    activate Strat
    
    loop Para cada mês da simulação
        Strat->>VO: currentBalance.multiply(rate)
        VO-->>Strat: interestAmount
        Strat->>VO: currentBalance.add(interestAmount)
        VO-->>Strat: finalBalance
        Strat->>Sim: Instancia SimulationStep(mês, inicial, juros, final)
    end
    
    Strat-->>Sim: Lista de SimulationStep (Memória de Cálculo)
    deactivate Strat

    Sim->>Sim: Consolida saldo final e calcula somatório total de juros
    Sim->>Val: Valida integridade e invariantes de todos os parâmetros
    Sim->>Sim: Executa cópia defensiva da lista de passos (List.copyOf)
    Sim-->>Client: Instância consolidada de Simulation
    deactivate Sim
```

---

## 💾 Camada de Persistência (Repository Layer - Subpacote `repository`)

Seguindo os princípios do **DDD Pragmático**, a persistência é desacoplada do modelo puro de domínio. A camada de infraestrutura e persistência lida com o mapeamento físico no banco de dados **H2 Database** e realiza as transições de estado por meio de entidades JPA e repositórios baseados no **Hibernate com Panache**.

```mermaid
graph LR
    subgraph com.simulador.financiamento.repository [Camada de Persistência]
        Repo[SimulationRepository]
        Entity[SimulationEntity]
        StepEntity[SimulationStepEntity]
    end

    subgraph com.simulador.financiamento.domain.simulation [Domínio Puro]
        Domain[Simulation]
    end

    Repo -. "Gerencia" .-> Entity
    Entity -- "OneToMany (Cascade.ALL)" --> StepEntity
    Entity -- "fromDomain() / toDomain()" --> Domain
```

### 1. Entidades Relacionais JPA
* **`SimulationEntity` (JPA Entity - `@Table(name = "simulation")`)**: Representação da raiz do agregado no banco de dados.
  * Mantém o relacionamento `@OneToMany` com `SimulationStepEntity` utilizando cascateamento total (`CascadeType.ALL` e `orphanRemoval = true`). Isso garante que a exclusão ou modificação na simulação se reflita automaticamente nas suas parcelas, assegurando a consistência lógica.
  * Contém mapeadores de domínio bidirecionais: `fromDomain()` mapeia o record imutável rico do domínio em uma entidade JPA mutável para inserção física, e `toDomain()` reconstrói o modelo de negócio puro.
* **`SimulationStepEntity` (JPA Entity - `@Table(name = "simulation_step")`)**: Representação relacional de cada mês da evolução detalhada (memória de cálculo), contendo chaves estrangeiras apropriadas e indexação no banco de dados.

### 2. Padrão Repository com Panache
* **`SimulationRepository`**: Repositório encarregado de encapsular a persistência física. Estende `PanacheRepositoryBase<SimulationEntity, String>` para gerenciar de forma nativa e limpa chaves primárias do tipo String (UUID), oferecendo métodos robustos de consulta sem poluir a camada de serviço com SQL/HQL.

### 3. Migração de Banco de Dados com Flyway
* **`V1.0.0__Init.sql`**: Executado automaticamente na inicialização da aplicação, criando as tabelas relacionais com precisões matemáticas estritas de juros e quantias monetárias:
  * Campo `interest_rate`: Decimal com **escala 8** (`DECIMAL(18, 8)`) para preservar integralmente a precisão das taxas de juros exigida pelo BACEn.
  * Valores monetários (`principal_amount`, `final_balance`, etc.): Decimal com **escala 2** (`DECIMAL(18, 2)`).
  * Exclusão física das parcelas vinculadas por meio de chave estrangeira com `ON DELETE CASCADE`.

---

## 📝 Documentação Exaustiva (JavaDocs)

A fim de fornecer clareza máxima e guiar os avaliadores, **todas as classes, records, construtores e métodos públicos do domínio foram documentados com JavaDocs exaustivos em português**. Cada método detalha o comportamento esperado, as validações Fail-Fast aplicadas e as exceções que podem ser lançadas.

---

## 🚀 Como Executar Localmente

### Pré-requisitos
* **Java 25 (SDK instalada localmente)**
* **Maven 3.9+**

### Modo de Desenvolvimento (Quarkus Dev Mode)
Para rodar a aplicação localmente com suporte a recarregamento dinâmico (*Hot Reload*):
```bash
./mvnw quarkus:dev
```
A API estará disponível em `http://localhost:8080`.

---

## 🧪 Qualidade e Testes Automatizados (TDD & Testes Integrados)

Toda a lógica da camada de domínio foi desenvolvida com foco total em cobertura e qualidade utilizando TDD. Os testes unitários do domínio são puros e executados de forma extremamente rápida, enquanto os testes integrados da persistência validam o banco de dados.

### Executar a Suíte de Testes
Para executar todos os **25 testes** (23 unitários puros do domínio + 2 testes integrados de banco de dados):
```bash
./mvnw clean test
```

### Testes Integrados de Persistência com Banco H2
* **`SimulationRepositoryTest`**: Teste anotado com `@QuarkusTest` que roda sobre o banco em memória H2 no profile `%test`.
* Valida a inserção em cascata (salva a raiz do agregado e insere automaticamente todos os passos associados), a reconstrução correta do domínio imutável a partir do banco e a exclusão em cascata física.

### Verificação do JaCoCo (Cobertura > 80%)
A validação de compilação, empacotamento e integridade dos limites de cobertura do JaCoCo é executada via:
```bash
./mvnw clean verify
```
Nossos testes cobrem **100% de linhas e caminhos lógicos** das classes de domínio e da persistência, superando amplamente a barreira eliminatória de 80% estabelecida no projeto.

---

## 📊 Observabilidade e Especificações
* **Métricas Locais (Micrometer/Prometheus):** `http://localhost:8080/q/metrics`
* **Especificação OpenAPI (SmallRye OpenAPI):** `http://localhost:8080/q/openapi`
