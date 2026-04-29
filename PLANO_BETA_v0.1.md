# Plano de Lançamento v0.1 Beta — Broder

> Avaliação de prontidão para lançamento público (LinkedIn beta) com épicos e user stories detalhadas.

---

## Status Geral

**Veredito: 🟢 PRONTO PARA BETA — 8 de 8 bloqueadores críticos resolvidos. Todos os Should-Have concluídos exceto screenshots reais.**

Base sólida, testes passando (55 backend + 20 frontend), observabilidade completa (health checks + métricas Prometheus), notificações via webhook ativas, deploy via Docker Compose funcional. SQLite é usado em todos os ambientes por design (zero-config, leve). Único item remanescente são screenshots reais no README (requer execução da stack completa).

---

## Épico 1: Fundação e Primeira Impressão

> *"Um novo visitante deve conseguir entender, clonar e rodar o Broder em menos de 5 minutos."*

### Story 1.1 — Docker Compose Unificado

**Como** desenvolvedor interessado no Broder  
**Quero** subir toda a stack com um único comando  
**Para que** eu possa testar o projeto sem configurar manualmente 4 serviços

**Critérios de Aceite:**
- [x] Existe `docker-compose.yml` na raiz do projeto
- [x] `docker compose up` sobe: Broder backend (8080), frontend nginx (3000), Prometheus (9090), ref app (8181)
- [x] O frontend nginx faz proxy reverso para `/api` → backend:8080
- [x] O backend aponta para `http://prometheus:9090` (nome do serviço no compose)
- [x] O CORS do backend aceita `http://localhost:3000` via profile ou variável de ambiente
- [x] `README.md` na raiz exibe o comando `docker compose up` como instrução principal

---

### Story 1.2 — Repositório Git Limpo

**Como** mantenedor do projeto  
**Quero** que o repositório esteja limpo de arquivos desnecessários  
**Para que** a primeira impressão de quem clona seja profissional

**Critérios de Aceite:**
- [x] `.DS_Store` removido do git e adicionado ao `.gitignore`
- [x] Todos os arquivos relevantes estão commitados (CSS, TSX, fontes)
- [x] Branches obsoletas locais e remotas removidas (ou mergeadas)
- [x] `data.db` e outros artefatos de build/runtime não estão rastreados
- [x] `git status` na branch principal retorna "working tree clean"

---

### Story 1.3 — README com Identidade Visual

**Como** visitante do repositório no GitHub  
**Quero** ver um README claro com screenshots e instruções  
**Para que** eu entenda o valor do Broder em 30 segundos

**Critérios de Aceite:**
- [x] README.md na raiz contém: descrição do produto (1 parágrafo), screenshot do builder, screenshot do dashboard, screenshot do histórico
- [x] Instruções de execução: `docker compose up` (recomendado) e modo dev (backend `./mvnw quarkus:dev`, frontend `pnpm dev`)
- [x] Lista de funcionalidades (bullet points)
- [x] Stack tecnológica (Quarkus, React, Prometheus, SQLite)
- [x] `index.html` do frontend tem `<title>` = "Broder — JVM Alerting" e favicon customizado (ou sem favicon do Vite)
- [x] README do frontend não é mais o template padrão do Vite

---

## Épico 2: Testes Automatizados

> *"O código deve ter cobertura mínima de testes para garantir confiança em mudanças."*

### Story 2.1 — Testes Unitários do Domínio

**Como** desenvolvedor do backend  
**Quero** testes unitários para as regras de domínio críticas  
**Para que** eu possa refatorar com segurança

**Critérios de Aceite:**
- [x] `ComparisonOperatorTest`: cobre `GT`, `LT`, `GTE`, `LTE`, `EQ` incluindo tolerância do `EQ`
- [x] `AlarmConditionTest`: valida construção com query vazia (deve lançar exceção), threshold inválido (deve lançar exceção), caso válido
- [x] `AlarmTest`: cobre `evaluate()` para transições `RESOLVED→FIRING`, `FIRING→RESOLVED`, `ACTIVE→FIRING`
- [x] `PromQLBuilderTest`: cobre build com `rate()`, `histogram_quantile`, query simples com filtros, query malformada com braces

---

### Story 2.2 — Teste de Integração da API de Alarmes

**Como** desenvolvedor do backend  
**Quero** pelo menos um teste de integração end-to-end para alarmes  
**Para que** eu valide que a API REST funciona corretamente

**Critérios de Aceite:**
- [x] `@QuarkusTest` em `AlarmResourceTest` ou similar
- [x] Criação de alarme via `POST /alarms` retorna 201 e body contém ID gerado
- [x] `GET /alarms/{id}` retorna o alarme criado
- [x] `PUT /alarms/{id}` atualiza o alarme e retorna 200
- [x] `DELETE /alarms/{id}` retorna 204 e alarme não é mais encontrado
- [x] Banco de dados em memória (`quarkus.datasource.jdbc.url=jdbc:sqlite::memory:`) usado nos testes
- [x] Testes rodam com `./mvnw test` sem falhas

---

### Story 2.3 — Testes Unitários do Frontend

**Como** desenvolvedor do frontend  
**Quero** um framework de testes configurado e pelo menos um teste rodando  
**Para que** eu possa garantir que componentes críticos renderizam corretamente

**Critérios de Aceite:**
- [x] Vitest (ou Jest) configurado no `broder-front` com suporte a React Testing Library
- [x] Teste para `AlarmCard`: recebe props e renderiza nome, status e severidade
- [x] Teste para `useOnboarding` hook: retorna `showTour=true` na primeira execução e `false` após dismiss
- [x] `pnpm test` (ou equivalente) roda os testes com sucesso
- [ ] Testes inclusos no pipeline de CI (quando implementado)

---

## Épico 3: Produção e Observabilidade

> *"O Broder deve ser capaz de operar em ambiente produtivo e monitorar a si mesmo."*

### Story 3.1 — Health Checks

**Como** operador que deploya o Broder em Kubernetes  
**Quero** endpoints de health check padronizados  
**Para que** o orquestrador saiba se a aplicação está saudável

**Critérios de Aceite:**
- [x] `quarkus-smallrye-health` adicionado ao `pom.xml`
- [x] `GET /q/health` retorna 200 com status `UP`
- [x] `GET /q/health/ready` retorna 200 quando a aplicação está pronta para receber tráfego
- [x] `GET /q/health/live` retorna 200 quando a aplicação está viva
- [x] Health check inclui verificação de conectividade com o banco de dados
- [x] Documentado no README

---

### Story 3.2 — Métricas da Aplicação

**Como** operador do Broder  
**Quero** métricas JVM e de negócio expostas no formato Prometheus  
**Para que** eu possa monitorar a saúde da ferramenta de monitoramento

**Critérios de Aceite:**
- [x] `quarkus-micrometer-registry-prometheus` adicionado ao `pom.xml`
- [x] `GET /q/metrics` retorna métricas no formato Prometheus text
- [x] Métricas incluem: JVM memory, JVM GC, HTTP request duration/count (Micrometer padrão)
- [x] Métrica customizada: `broder_alarms_evaluated_total` (counter de avaliações do scheduler)
- [x] Métrica customizada: `broder_alarms_firing_total` (gauge de alarmes atualmente FIRING)
- [x] Métrica customizada: `broder_scheduler_duration_seconds` (histogram do tempo de cada ciclo do scheduler)

---

### Story 3.3 — SQLite como Banco Padrão (Decisão de Arquitetura)

**Decisão:** Broder usa SQLite em **todos** os ambientes (dev e produção) para manter a ferramenta leve, zero-config e fácil de operar.

**Rationale:**
- O foco do Broder é ser uma alternativa *leve* a stacks complexas como Grafana + Alertmanager.
- SQLite é suficiente para o volume de dados de alarmes e histórico (tipicamente milhares de linhas, não milhões).
- Elimina a necessidade de gerenciar um serviço de banco separado em produção.
- O arquivo `./data/data.db` é montado como volume Docker no `docker-compose.yml` para persistência.

**Critérios de Aceite:**
- [x] `application.yaml` usa SQLite (`db-kind: sqlite`)
- [x] Banco persistido via volume no Docker Compose
- [x] Documentado no README que SQLite é usado em todos os ambientes
- [ ] (Opcional) Backup automatizado do arquivo `.db` documentado

---

## Épico 4: Notificações

> *"O Broder deve ser capaz de notificar canais externos quando um alarme dispara."*

### Story 4.1 — Webhook Genérico de Notificação

**Como** usuário do Broder  
**Quero** configurar um webhook que recebe um POST quando um alarme dispara  
**Para que** eu possa integrar com Slack, Discord, PagerDuty ou qualquer outro sistema

**Critérios de Aceite:**
- [x] Novo campo `webhookUrl` no `Alarm` (ou config global no `application.yaml`)
- [x] `AlarmNotificationService` envia `POST` para o webhook quando alarme transiciona para `FIRING`
- [x] Payload JSON contém: `alarmName`, `alarmId`, `status`, `severity`, `timestamp`, `currentValue`, `message`
- [x] Se o webhook falhar (timeout, 5xx), o erro é logado mas **não** impede o registro do histórico
- [x] Se `webhookUrl` não estiver configurado, o comportamento continua como antes (log only)
- [x] Timeout configurável (default 5s)

---

### Story 4.2 — Integração Webhook Slack/Discord (Exemplo)

**Como** usuário do Broder  
**Quero** um exemplo funcional de integração com Slack  
**Para que** eu veja o "wow factor" do produto funcionando

**Critérios de Aceite:**
- [ ] Documentação no README explicando como criar um Incoming Webhook no Slack
- [ ] Payload compatível com o formato esperado pelo Slack (`text` field)
- [ ] Screenshot ou GIF mostrando um alerta chegando no Slack

---

## Épico 5: Polimento de UX

> *"A experiência do usuário deve ser confiável e consistente."*

### Story 5.1 — Confirmação de Deleção

**Como** usuário do dashboard de alarmes  
**Quero** uma confirmação antes de deletar um alarme  
**Para que** eu não apague acidentalmente uma configuração importante

**Critérios de Aceite:**
- [x] Ao clicar em "Delete" no `AlarmCard`, um modal de confirmação aparece
- [x] Modal exibe: "Are you sure you want to delete alarm 'X'?" + botões "Cancel" e "Delete"
- [x] Alarme só é deletado após confirmação
- [x] Modal pode ser fechado com ESC ou clicando fora
- [x] Teste unitário cobre o fluxo de cancelamento

---

### Story 5.2 — Error Boundaries no React

**Como** usuário do Broder  
**Quero** que a aplicação não quebre completamente se um componente falhar  
**Para que** eu possa continuar usando outras partes do sistema

**Critérios de Aceite:**
- [x] `ErrorBoundary` implementado no nível da aplicação (router ou `App.tsx`)
- [x] Erros em componentes filhos exibem uma mensagem amigável: "Something went wrong. Please reload the page."
- [x] Erro é logado no console para debug
- [x] Teste verifica que um componente que lança erro não derruba a aplicação inteira

---

### Story 5.3 — Idioma Consistente (Inglês)

**Como** visitante internacional no LinkedIn  
**Quero** que toda a interface esteja em inglês  
**Para que** o produto pareça pronto para o mercado global

**Critérios de Aceite:**
- [x] Todas as labels da UI traduzidas para inglês: "Alarms", "History", "Create Alarm", "Builder", etc.
- [x] Mensagens de erro da API em inglês (ou pelo menos consistentes)
- [x] README principal em inglês (ou versão bilingue com EN no topo)
- [x] Documentação interna (AGENTS.md) pode permanecer em PT (é para devs)

---

## Resumo por Prioridade

| # | Story | Épico | Bloqueador? | Status |
|---|-------|-------|-------------|--------|
| 1 | Docker Compose Unificado | 1 | ✅ CRÍTICO | **Concluído** |
| 2 | Repositório Git Limpo | 1 | ✅ CRÍTICO | **Concluído** |
| 3 | README com Identidade | 1 | ✅ CRÍTICO | **Concluído** (screenshots placeholder) |
| 4 | Testes Unitários Domínio | 2 | ✅ CRÍTICO | **Concluído** — 55 testes passando |
| 5 | Teste Integração Alarmes | 2 | ✅ CRÍTICO | **Concluído** |
| 6 | Testes Unitários Frontend | 2 | ✅ CRÍTICO | **Concluído** |
| 7 | Health Checks | 3 | ✅ CRÍTICO | **Concluído** |
| 8 | Webhook Genérico | 4 | ✅ CRÍTICO | **Concluído** |
| 9 | Métricas Micrometer | 3 | 🟡 Should Have | **Concluído** |
| 10 | SQLite em todos os ambientes | 3 | 🟡 Should Have | **Concluído** |
| 11 | Confirmação de Deleção | 5 | 🟡 Should Have | **Concluído** |
| 12 | Error Boundaries | 5 | 🟡 Should Have | **Concluído** |
| 13 | Idioma Consistente | 5 | 🟡 Should Have | **Concluído** |
| 14 | Webhook Slack (exemplo) | 4 | 🔵 Nice to Have | Pendente |

---

## Notas para o Time

1. **Idioma:** O público-alvo do LinkedIn é global. Recomendo inglês para UI pública, mantendo documentação interna (AGENTS.md) em português se preferir.
2. **Webhook:** Definir se será configuração **por alarme** (cada um tem seu webhook) ou **global** (um webhook para todo o sistema). Recomendo **por alarme** para flexibilidade.
3. **Banco de dados:** SQLite é usado em todos os ambientes (dev e produção) como decisão de arquitetura para manter o Broder leve e zero-config. Não há planos de suportar PostgreSQL nativamente.
4. **Prazo estimado:** 3–5 dias de trabalho focado para resolver todos os bloqueadores críticos.

---

*Documento criado em: 2026-04-28*
*Próxima revisão: após resolução dos 8 bloqueadores críticos*
