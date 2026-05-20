# Loja com Microsservicos

Este repositório implementa uma pequena loja baseada em microsserviços com `Spring Boot`, `PostgreSQL`, `Docker Compose` e `Thymeleaf`. O conteúdo deste README foi estruturado a partir do roteiro em `main.tex`, que descreve a construção do sistema passo a passo.

## Visao geral

O sistema é dividido em quatro aplicações:

- `clientes-service`: cadastro e consulta de clientes.
- `estoque-service`: cadastro de produtos, saldo em estoque e débito de itens.
- `pedidos-service`: criação e conclusão de pedidos, integrando clientes e estoque.
- `web-ms`: interface web que consome as APIs REST dos demais serviços.

Cada serviço possui uma responsabilidade isolada. A interface web não persiste dados diretamente: ela apenas chama os microsserviços responsáveis por cada domínio.

## Fluxo de negocio

O fluxo principal da aplicação é:

1. Cadastrar um cliente.
2. Cadastrar um produto no estoque.
3. Criar um pedido com um ou mais itens.
4. Concluir o pedido.
5. Debitar automaticamente o estoque ao concluir.

Isso reflete a proposta central do projeto: separar cadastro de clientes, gestão de estoque, processamento de pedidos e interface do usuário.

## Arquitetura

| Servico | Responsabilidade |
| --- | --- |
| `clientes-service` | CRUD de clientes |
| `estoque-service` | CRUD de produtos e controle de saldo |
| `pedidos-service` | Orquestração de pedidos e integração entre serviços |
| `web-ms` | Interface web com Thymeleaf |

## Como o codigo esta organizado

O roteiro em `main.tex` organiza a implementação em blocos bem definidos, e essa estrutura se reflete no código do projeto:

- `clientes-service/`: aplicação Spring Boot focada no domínio de clientes.
- `estoque-service/`: aplicação Spring Boot focada em produtos e quantidade disponível.
- `pedidos-service/`: aplicação Spring Boot responsável pelas regras de pedido e chamadas HTTP para os outros serviços.
- `web-ms/`: aplicação MVC com Thymeleaf para formularios, listagens e conclusão de pedidos.
- `docker-compose.yml`: orquestra os serviços e três bancos PostgreSQL separados.
- `main.tex`: roteiro de prática que explica as decisões, arquivos e testes do projeto.

## Padrao tecnico usado nos microsservicos

Os serviços REST seguem o mesmo desenho geral:

- classe principal Spring Boot para inicialização;
- `application.properties` com porta e conexão via variáveis de ambiente;
- entidades JPA para persistência;
- DTOs para entrada e saída de dados;
- repositórios Spring Data JPA;
- camada de service com regras de negócio;
- controllers REST para expor as rotas.

No `pedidos-service`, a parte mais importante é a integração entre serviços. Ele valida cliente, cria pedidos e, na conclusão, conversa com o `estoque-service` para debitar os itens comprados.

## Infraestrutura com Docker

O `docker-compose.yml` sobe:

- `3` bancos PostgreSQL, um para cada microsserviço de domínio;
- `3` microsserviços REST;
- `1` aplicação web.

Portas publicadas no host:

- `9005`: `clientes-service`
- `9006`: `estoque-service`
- `9007`: `pedidos-service`
- `9008`: `web-ms`

Essa separação simplifica a prática e evita acoplamento de banco entre os serviços.

## Como executar

Requisitos esperados no roteiro:

- `Java 17`
- `Maven 3.9+`
- `Docker`
- `Docker Compose`

Para subir todo o ambiente:

```bash
docker compose up --build -d
```

Para verificar os containers:

```bash
docker ps
```

Depois disso, a interface web deve ficar disponível em:

```text
http://localhost:9008
```

## Testes basicos da API

O roteiro em `main.tex` propõe validar o sistema nesta ordem:

1. Criar cliente em `clientes-service`.
2. Criar produto em `estoque-service`.
3. Criar pedido em `pedidos-service`.
4. Concluir o pedido.
5. Consultar o estoque e confirmar o débito.

Exemplo de criação de cliente:

```bash
curl -i -X POST http://localhost:9005/api/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nomeCompleto": "Maria da Silva",
    "cpf": "12345678901",
    "endereco": "Rua A, 100",
    "telefone": "85999990000"
  }'
```

Exemplo de criação de pedido:

```bash
curl -i -X POST http://localhost:9007/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "itens": [
      {
        "produtoId": 1,
        "quantidade": 2
      }
    ]
  }'
```

## O que `main.tex` explica

Além do código em si, `main.tex` documenta:

- a motivação da arquitetura de microsserviços;
- a criação dos arquivos base de cada aplicação;
- a lógica de entidades, DTOs, services e controllers;
- a composição via Docker;
- o papel do `web-ms`;
- testes com `curl`;
- problemas comuns e formas de diagnóstico.

Em outras palavras, o arquivo funciona como documentação técnica e roteiro didático do projeto.
