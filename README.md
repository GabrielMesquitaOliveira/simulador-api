# Simulador de Financiamentos API

API de alta performance para simulação de produtos financeiros, construída com **Java 25** e **Quarkus**.

## 🚀 Como Executar Localmente

### Pré-requisitos
- JDK 25 instalado
- Maven 3.9+

### Modo de Desenvolvimento
Para rodar a aplicação com *Hot Reload* e banco de dados H2 automático:
```bash
./mvnw quarkus:dev
```
A API estará disponível em `http://localhost:8080`.

## 🧪 Qualidade e Testes

### Execução da Suíte de Testes e Cobertura
O projeto utiliza **Jacoco** com uma política de **fail-fast** para garantir que a cobertura de código nunca seja inferior a **80%**.

Para rodar os testes e gerar o relatório de cobertura:
```bash
./mvnw clean verify
```

O relatório detalhado pode ser encontrado em:
`target/site/jacoco/index.html`

### Observabilidade
- **Métricas (Prometheus):** `http://localhost:8080/q/metrics`
- **OpenAPI/Swagger:** `http://localhost:8080/q/openapi`
