# SiGOH — Sistema de Gestão de Ocupação Hospitalar

Aplicação web para controle de ocupação de leitos hospitalares em tempo real, desenvolvida
como trabalho da disciplina de Programação Web.

Este repositório contém a **fundação estrutural do projeto**: a arquitetura completa já
definida e implementada de ponta a ponta, com um recorte reduzido do domínio. Todas as
decisões técnicas que valerão para o sistema final já estão tomadas e materializadas em
código — o que falta é extensão de funcionalidade, não redefinição de arquitetura.

---

## 1. O problema

Hospitais perdem visibilidade sobre a própria ocupação. Leitos ficam ociosos enquanto
pacientes aguardam, porque a informação de qual leito está livre circula em fichas de papel,
planilhas paralelas ou pelo rádio da equipe. A cada troca de turno, o retrato da ocupação se
desatualiza; quem precisa decidir sobre capacidade decide com dados de horas atrás.

O SiGOH centraliza esse controle: cada mudança de status de leito é registrada uma única vez,
com autoria e horário, e aparece imediatamente para todos que estão vendo o mapa.

### Regras de negócio

**Estrutura física.** O hospital se organiza em três níveis: `Bloco → Enfermaria → Leito`.
Um leito pertence a exatamente uma enfermaria, que pertence a exatamente um bloco.

**Ciclo de vida do leito.** Um leito assume um de quatro estados:

| Status       | Significado                                |
| ------------ | ------------------------------------------ |
| `DISPONIVEL` | Livre para receber paciente                |
| `OCUPADO`    | Em uso                                     |
| `LIMPEZA`    | Higienização após alta, ainda indisponível |
| `BLOQUEADO`  | Fora de operação (manutenção, interdição)  |

O ciclo operacional normal é `DISPONIVEL → OCUPADO → LIMPEZA → DISPONIVEL`. `BLOQUEADO` é um
estado administrativo que sai desse ciclo e só retorna por ação explícita.

**Auditoria.** Toda mudança de status gera um registro imutável em `historico_leitos`
(status anterior, status novo, autor, horário). A tabela é _append-only_: nenhuma rotina da
aplicação a atualiza ou remove.

**Taxa de ocupação.** Calculada no servidor, sempre derivada dos leitos (nunca armazenada
no leito, para não haver risco de o agregado divergir da origem). Leitos `BLOQUEADO` são
excluídos do denominador — não estão disponíveis para uso, então contá-los distorceria o
indicador para baixo.

### Perfis de acesso

| Ator          | Perfil              | Pode fazer                                                                         |
| ------------- | ------------------- | ---------------------------------------------------------------------------------- |
| Visitante     | — (não autenticado) | Apenas a tela de login                                                             |
| Usuário Comum | `ROLE_USER`         | Consultar o mapa de leitos e as taxas de ocupação                                  |
| Administrador | `ROLE_ADMIN`        | Tudo do usuário comum + administrar blocos, enfermarias e leitos, e alterar status |

---

## 2. Arquitetura

### Estilo: cliente-servidor desacoplado (API REST + SPA)

Back-end e front-end são projetos independentes que se comunicam exclusivamente por HTTP/JSON.
O acoplamento entre eles é apenas o contrato de dados, não a tecnologia — o mesmo back-end
poderia atender um aplicativo móvel sem alteração alguma.

```
┌────────────────────┐   HTTP / JSON    ┌──────────────────────┐    JDBC    ┌──────────────┐
│  SPA React + TS     │ ───────────────▶ │  API Spring Boot      │ ─────────▶ │  PostgreSQL   │
│  (navegador)        │ ◀─────────────── │  (Spring Security)    │ ◀───────── │              │
└────────────────────┘                   └──────────────────────┘             └──────────────┘
          ▲                                          │
          └──────── WebSocket / STOMP ───────────────┘
                    (push de mudanças de status)
```

### Camadas do back-end

Organização **por funcionalidade** (_package by feature_): cada domínio concentra suas próprias
classes num único pacote, em vez de espalhá-las por pastas genéricas de controllers, services e
repositories. Localizar tudo sobre "leito" é olhar uma pasta só.

```
com.sigoh.api
├── config/        Segurança (JWT, CORS), WebSocket, carga de dados de desenvolvimento
├── common/        Enums de domínio e tratamento centralizado de exceções
├── auth/          Autenticação (emissão de token)
├── usuario/       Usuário e integração com o Spring Security
├── bloco/         ─┐
├── enfermaria/     ├─ Estrutura física, com CRUD completo
├── leito/         ─┘
├── historico/     Trilha de auditoria (append-only)
├── ocupacao/      Cálculo de taxas e rotina de snapshot diário
└── realtime/      Publicação de eventos no canal WebSocket
```

Dentro de cada pacote, o fluxo é sempre o mesmo:

```
Controller  →  recebe a requisição, valida o DTO de entrada, devolve o DTO de saída
    ↓
Service     →  regra de negócio e fronteira transacional
    ↓
Repository  →  acesso a dados (Spring Data JPA)
    ↓
Entity      →  mapeamento objeto-relacional
```

A entidade JPA **nunca** é devolvida diretamente na resposta HTTP. Tudo passa por um `Mapper`
que a converte em DTO, para que mudanças internas do modelo não quebrem o contrato público
da API.

### Camadas do front-end

```
src/
├── pages/       Telas (Login, MapaLeitos, GestaoEstrutura)
├── services/    Comunicação com a API — um módulo por domínio
├── context/     Estado de autenticação compartilhado
├── hooks/       Assinatura do canal de tempo real
├── routes/      Proteção de rotas por perfil
├── types/       Contratos espelhando os DTOs do back-end
└── styles/      Folha de estilo única, com paleta verificada para contraste
```

Nenhum componente chama `fetch` diretamente: tudo passa por `services/api.ts`, que centraliza
URL base, cabeçalho de autenticação e tratamento de erro.

---

## 3. Decisões técnicas e suas justificativas

### PostgreSQL como banco de dados

O modelo é inerentemente relacional: blocos contêm enfermarias, que contêm leitos, e o
histórico referencia leitos e usuários. Chaves estrangeiras e `CHECK constraints` garantem no
próprio banco invariantes que, de outra forma, dependeriam de disciplina do código da aplicação.

Além disso, as consultas mais importantes do sistema são **agregações** (quantos leitos em cada
status, por bloco) — exatamente onde SQL é mais forte. Essas contagens são feitas pelo banco,
não trazendo linhas para a memória da aplicação só para contá-las.

O `docker-compose.yml` sobe o banco pronto, então nenhum integrante precisa instalar e
configurar PostgreSQL na própria máquina.

### Flyway para versionamento de schema

O schema é tratado como código: cada alteração é um script SQL numerado e versionado junto
com a aplicação (`db/migration/V1__schema_inicial.sql`). O Flyway mantém uma tabela de
controle com o que já foi aplicado e o _checksum_ de cada script — se alguém editar uma
migration já executada, a aplicação recusa subir em vez de aplicar silenciosamente algo
diferente do que está em produção.

A alternativa seria deixar o Hibernate gerar o schema sozinho (`ddl-auto: update`). Foi
descartada: não há histórico de mudanças, não há revisão possível em _code review_, e uma
alteração mal interpretada pode causar perda de dados. A configuração usada é
`ddl-auto: validate` — o Hibernate apenas **confere** se as entidades batem com as tabelas e
falha na inicialização se divergirem, o que expõe o erro cedo em vez de na primeira consulta.

### Autenticação stateless com JWT

O servidor não guarda sessão. O token é auto-contido e assinado (HMAC-SHA): a cada
requisição, o servidor apenas confere a assinatura, sem consultar banco nem memória de
sessão. Isso é o que permite, no futuro, rodar várias instâncias da API atrás de um
balanceador sem precisar de sessão compartilhada.

Senhas são armazenadas com **BCrypt**, que embute _salt_ por hash e tem custo computacional
configurável — não se guarda senha em texto claro nem hash simples.

### Autorização por papéis, imposta no servidor

A regra é: leitura liberada a qualquer autenticado, escrita restrita a `ADMIN`. Ela vive em
`SecurityConfig`, no servidor.

O front-end também esconde botões que o usuário não pode usar, mas isso é **conveniência de
interface, não mecanismo de segurança** — código que roda no navegador do usuário pode ser
inspecionado e contornado. Uma requisição `DELETE` forjada por um usuário comum é recusada
com HTTP 403 mesmo que ele nunca tenha visto o botão.

### WebSocket (STOMP) para tempo real

O mapa de leitos é olhado por várias pessoas simultaneamente. Sem um canal de _push_, cada
tela só descobriria uma mudança ao recarregar — ou por _polling_, que desperdiça requisições
e ainda assim chega atrasado.

A API publica um evento em `/topic/ocupacao` a cada mudança de status; o front-end assina esse
tópico e atualiza a tela sem recarregar. O _broker_ usado é o embutido do Spring, que mantém
as assinaturas em memória: suficiente para uma instância da aplicação. Escalar para várias
exigiria um broker externo (RabbitMQ, ActiveMQ) — decisão deliberadamente adiada até que essa
necessidade exista de fato.

### Ponto único de mudança de status

Só existe um caminho para alterar o status de um leito: `LeitoService.aplicarMudancaStatus()`.
Ele grava o leito, grava o registro de auditoria e publica o evento de tempo real, tudo dentro
da **mesma transação** — ou as três coisas acontecem, ou nenhuma acontece.

Essa concentração é intencional: se cada controller pudesse alterar status por conta própria,
mais cedo ou mais tarde alguma alteração escaparia da auditoria.

### Exclusão lógica de leitos

Excluir um leito o marca como inativo em vez de removê-lo. O histórico de auditoria já
registrado continua apontando para um registro existente — apagar a linha quebraria a trilha.
Blocos e enfermarias, por não serem referenciados pelo histórico, têm exclusão física, mas
protegida: o servidor recusa excluir um bloco que ainda tenha enfermarias, para não deixar
leitos órfãos.

### Snapshot diário de ocupação

Uma rotina agendada (`@Scheduled`) grava, ao fim de cada dia, a ocupação consolidada de cada
bloco. Relatórios históricos consultam essa tabela em vez de reprocessar todo o histórico de
eventos a cada consulta. A rotina é idempotente — rodar duas vezes no mesmo dia não duplica
registros.

### Acessibilidade tratada desde o início

Requisitos de acessibilidade são muito mais caros de adaptar depois do que de nascer junto
com a interface. O que já está aplicado:

- Marcação semântica: `<header>`, `<nav>`, `<main>`, `<footer>`, `<table>` com `<th scope>` e `<caption>`
- Todo campo de formulário tem `<label for>` associado; rótulos em linha usam classe visualmente oculta mas legível por leitor de tela
- Mensagens de erro com `role="alert"`, anunciadas assim que surgem
- Indicador de foco visível (`:focus-visible`) em todo elemento interativo
- Paleta com razão de contraste verificada contra WCAG AA (≥ 4.5:1), anotada no CSS
- Status de leito comunicado por **cor e texto** — cor sozinha não é o único veículo de informação

### Sem framework de UI

O CSS é escrito à mão. Isso permitiu controlar e verificar o contraste de cada combinação de
cores em vez de herdar uma paleta de terceiros, e mantém a superfície de dependências pequena.

---

## 4. Escopo desta entrega

Esta é a etapa de **protótipo estrutural**. A estratégia adotada foi construir a arquitetura
inteira funcionando de ponta a ponta sobre um recorte pequeno do domínio, em vez de construir
muitas telas sobre uma base ainda indefinida — assim qualquer funcionalidade nova é extensão
de um padrão já validado, não invenção de um novo.

**Já implementado:**

- Modelo de dados completo (usuários, blocos, enfermarias, leitos, histórico, snapshots) em migration versionada
- Autenticação JWT com dois perfis e autorização aplicada no servidor
- CRUD completo de blocos e enfermarias; CRUD de leitos com atualização de status auditada
- Cálculo de taxa de ocupação por bloco
- Canal de tempo real ponta a ponta (servidor publica, front-end reage)
- Rotina agendada de snapshot diário
- Tratamento centralizado de erros com formato de resposta padronizado
- Front-end com login, mapa de leitos e administração de blocos

**Próximos passos:**

- Telas de administração de enfermarias e leitos (o padrão de `GestaoEstrutura` se repete)
- Gráficos de ocupação (atual e histórico, sobre a tabela de snapshots)
- Tela de consulta ao histórico de um leito (o endpoint já existe)
- Visão mobile dedicada para atualização de status durante a ronda
- Persistência da sessão entre recarregamentos de página

---

## 5. Como executar

### Pré-requisitos

Java 17+, Maven, Node.js 18+ e Docker (para o banco).

### Banco de dados

```bash
docker compose up -d
```

Sobe um PostgreSQL em `localhost:5432` (base `sigoh`, usuário `sigoh`, senha `sigoh`).
O Flyway cria todas as tabelas automaticamente na primeira inicialização da API.

### Back-end

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Disponível em `http://localhost:8080`. O perfil `dev` carrega dados de demonstração
(dois usuários, dois blocos, três enfermarias e onze leitos):

| E-mail            | Senha      | Perfil       |
| ----------------- | ---------- | ------------ |
| `admin@sigoh.com` | `admin123` | `ROLE_ADMIN` |
| `user@sigoh.com`  | `user123`  | `ROLE_USER`  |

### Front-end

```bash
cd frontend
npm install
npm run dev
```

Disponível em `http://localhost:5173`, já apontando para a API local.
Para outro endereço, copie `.env.example` para `.env` e ajuste.

---

## 6. Endpoints da API

| Método                | Rota                                 | Perfil      | Descrição                      |
| --------------------- | ------------------------------------ | ----------- | ------------------------------ |
| `POST`                | `/auth/login`                        | público     | Autentica e devolve o token    |
| `GET`                 | `/api/blocos`                        | autenticado | Lista blocos                   |
| `POST` `PUT` `DELETE` | `/api/blocos`                        | `ADMIN`     | Administra blocos              |
| `GET`                 | `/api/enfermarias?blocoId=`          | autenticado | Lista enfermarias              |
| `POST` `PUT` `DELETE` | `/api/enfermarias`                   | `ADMIN`     | Administra enfermarias         |
| `GET`                 | `/api/leitos?blocoId=&enfermariaId=` | autenticado | Lista leitos                   |
| `GET`                 | `/api/leitos/{id}/historico`         | autenticado | Trilha de auditoria do leito   |
| `POST` `PUT` `DELETE` | `/api/leitos`                        | `ADMIN`     | Administra leitos              |
| `PATCH`               | `/api/leitos/{id}/status`            | `ADMIN`     | Altera o status (auditado)     |
| `PATCH`               | `/api/leitos/{id}/avancar-status`    | `ADMIN`     | Avança no ciclo operacional    |
| `GET`                 | `/api/ocupacao/blocos`               | autenticado | Taxa de ocupação por bloco     |
| `WS`                  | `/ws` → `/topic/ocupacao`            | —           | Canal de eventos em tempo real |

---

## 7. Equipe

| Integrante    | Contribuição  |
| ------------- | ------------- |
| _(preencher)_ | _(preencher)_ |

## 8. Links externos

## Repositório no GitHub -> https://github.com/monteirommd/sigoh-v2

## Protótipo no Figma -> https://www.figma.com/design/mXVev8UAVkNkknuRpBZl4W/Mapa-de-Leitos?node-id=0-1&t=fGTUPUsybmEpxAgd-1
