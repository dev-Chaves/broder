# Plano de Lançamento v0.1 Beta — Broder

> Avaliação de prontidão para lançamento público (LinkedIn beta) com épicos e user stories detalhadas.

---

## Status Geral

**Veredito: ❌ NÃO está pronto para lançamento público sem resolver os bloqueadores críticos.**

O projeto tem base sólida e visual impressionante, mas carece de testes, observabilidade, notificações reais, deploy simplificado e polimento de primeira impressão.

---

## Épico 1: Fundação e Primeira Impressão

> *"Um novo visitante deve conseguir entender, clonar e rodar o Broder em menos de 5 minutos."*

### Story 1.1 — Docker Compose Unificado

**Como** desenvolvedor interessado no Broder  
**Quero** subir toda a stack com um único comando  
**Para que** eu possa testar o projeto sem configurar manualmente 4 serviços

**Critérios de Aceite:**
- [ ] Existe `docker-compose.yml` na raiz do projeto
- [ ] `docker compose up` sobe: Broder backend (8080), frontend nginx (3000), Prometheus (9090), ref app (8181)
- [ ] O frontend nginx faz proxy reverso para `/api` → backend:8080
- [ ] O backend aponta para `http://prometheus:9090` (nome do serviço no compose)
- [ ] O CORS do backend aceita `http://localhost:3000` via profile ou variável de ambiente
- [ ] `README.md` na raiz exibe o comando `docker compose up` como instrução principal

---

### Story 1.2 — Repositório Git Limpo

**Como** mantenedor do projeto  
**Quero** que o repositório esteja limpo de arquivos desnecessários  
**Para que** a primeira impressão de quem clona seja profissional

**Critérios de Aceite:**
- [ ] `.DS_Store` removido do git e adicionado ao `.gitignore`
- [ ] Todos os arquivos relevantes estão commitados (CSS, TSX, fontes)
- [ ] Branches obsoletas locais e remotas removidas (ou mergeadas)
- [ ] `data.db` e outros artefatos de build/runtime não estão rastreados
- [ ] `git status` na branch principal retorna "working tree clean"

---

### Story 1.3 — README com Identidade Visual

**Como** visitante do repositório no GitHub  
**Quero** ver um README claro com screenshots e instruções  
**Para que** eu entenda o valor do Broder em 30 segundos

**Critérios de Aceite:**
- [ ] README.md na raiz contém: descrição do produto (1 parágrafo), screenshot do builder, screenshot do dashboard, screenshot do histórico
- [ ] Instruções de execução: `docker compose up` (recomendado) e modo dev (backend `./mvnw quarkus:dev`, frontend `pnpm dev`)
- [ ] Lista de funcionalidades (bullet points)
- [ ] Stack tecnológica (Quarkus, React, Prometheus, SQLite)
- [ ] `index.html` do frontend tem `<title>` = "Broder — JVM Alerting" e favicon customizado (ou sem favicon do Vite)
- [ ] README do frontend não é mais o template padrão do Vite

---

## Épico 2: Testes Automatizados

> *"O código deve ter cobertura mínima de testes para garantir confiança em mudanças."*

### Story 2.1 — Testes Unitários do Domínio

**Como** desenvolvedor do backend  
**Quero** testes unitários para as regras de domínio críticas  
**Para que** eu possa refatorar com segurança

**Critérios de Aceite:**
- [ ] `ComparisonOperatorTest`: cobre `GT`, `LT`, `GTE`, `LTE`, `EQ` incluindo tolerância do `EQ`
- [ ] `AlarmConditionTest`: valida construção com query vazia (deve lançar exceção), threshold inválido (deve lançar exceção), caso válido
- [ ] `AlarmTest`: cobre `evaluate()` para transições `RESOLVED→FIRING`, `FIRING→RESOLVED`, `ACTIVE→FIRING`
- [ ] `PromQLBuilderTest`: cobre build com `rate()`, `histogram_quantile`, query simples com filtros, query malformada com braces

---

### Story 2.2 — Teste de Integração da API de Alarmes

**Como** desenvolvedor do backend  
**Quero** pelo menos um teste de integração end-to-end para alarmes  
**Para que** eu valide que a API REST funciona corretamente

**Critérios de Aceite:**
- [ ] `@QuarkusTest` em `AlarmResourceTest` ou similar
- [ ] Criação de alarme via `POST /alarms` retorna 201 e body contém ID gerado
- [ ] `GET /alarms/{id}` retorna o alarme criado
- [ ] `PUT /alarms/{id}` atualiza o alarme e retorna 200
- [ ] `DELETE /alarms/{id}` retorna 204 e alarme não é mais encontrado
- [ ] Banco de dados em memória (`quarkus.datasource.jdbc.url=jdbc:sqlite::memory:`) usado nos testes
- [ ] Testes rodam com `./mvnw test` sem falhas

---

### Story 2.3 — Testes Unitários do Frontend

**Como** desenvolvedor do frontend  
**Quero** um framework de testes configurado e pelo menos um teste rodando  
**Para que** eu possa garantir que componentes críticos renderizam corretamente

**Critérios de Aceite:**
- [ ] Vitest (ou Jest) configurado no `broder-front` com suporte a React Testing Library
- [ ] Teste para `AlarmCard`: recebe props e renderiza nome, status e severidade
- [ ] Teste para `useOnboarding` hook: retorna `showTour=true` na primeira execução e `false` após dismiss
- [ ] `pnpm test` (ou equivalente) roda os testes com sucesso
- [ ] Testes inclusos no pipeline de CI (quando implementado)

---

## Épico 3: Produção e Observabilidade

> *"O Broder deve ser capaz de operar em ambiente produtivo e monitorar a si mesmo."*

### Story 3.1 — Health Checks

**Como** operador que deploya o Broder em Kubernetes  
**Quero** endpoints de health check padronizados  
**Para que** o orquestrador saiba se a aplicação está saudável

**Critérios de Aceite:**
- [ ] `quarkus-smallrye-health` adicionado ao `pom.xml`
- [ ] `GET /q/health` retorna 200 com status `UP`
- [ ] `GET /q/health/ready` retorna 200 quando a aplicação está pronta para receber tráfego
- [ ] `GET /q/health/live` retorna 200 quando a aplicação está viva
- [ ] Health check inclui verificação de conectividade com o banco de dados
- [ ] Documentado no README

---

### Story 3.2 — Métricas da Aplicação

**Como** operador do Broder  
**Quero** métricas JVM e de negócio expostas no formato Prometheus  
**Para que** eu possa monitorar a saúde da ferramenta de monitoramento

**Critérios de Aceite:**
- [ ] `quarkus-micrometer-registry-prometheus` adicionado ao `pom.xml`
- [ ] `GET /q/metrics` retorna métricas no formato Prometheus text
- [ ] Métricas incluem: JVM memory, JVM GC, HTTP request duration/count (Micrometer padrão)
- [ ] Métrica customizada: `broder_alarms_evaluated_total` (counter de avaliações do scheduler)
- [ ] Métrica customizada: `broder_alarms_firing_total` (gauge de alarmes atualmente FIRING)
- [ ] Métrica customizada: `broder_scheduler_duration_seconds` (histogram do tempo de cada ciclo do scheduler)

---

### Story 3.3 — Profile de Produção com PostgreSQL

**Como** operador que quer rodar Broder em produção  
**Quero** um profile `prod` configurado para PostgreSQL  
**Para que** eu não dependa do SQLite file-based

**Critérios de Aceite:**
- [ ] `application-prod.yaml` criado com configuração PostgreSQL
- [ ] `application.yaml` continua com SQLite (modo dev)
- [ ] Profile ativável via `quarkus.profile=prod` ou env var
- [ ] Instruções no README explicando como rodar com PostgreSQL
- [ ] (Opcional) Docker Compose inclui serviço PostgreSQL com profile `prod`

---

## Épico 4: Notificações

> *"O Broder deve ser capaz de notificar canais externos quando um alarme dispara."*

### Story 4.1 — Webhook Genérico de Notificação

**Como** usuário do Broder  
**Quero** configurar um webhook que recebe um POST quando um alarme dispara  
**Para que** eu possa integrar com Slack, Discord, PagerDuty ou qualquer outro sistema

**Critérios de Aceite:**
- [ ] Novo campo `webhookUrl` no `Alarm` (ou config global no `application.yaml`)
- [ ] `AlarmNotificationService` envia `POST` para o webhook quando alarme transiciona para `FIRING`
- [ ] Payload JSON contém: `alarmName`, `alarmId`, `status`, `severity`, `timestamp`, `currentValue`, `message`
- [ ] Se o webhook falhar (timeout, 5xx), o erro é logado mas **não** impede o registro do histórico
- [ ] Se `webhookUrl` não estiver configurado, o comportamento continua como antes (log only)
- [ ] Timeout configurável (default 5s)

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
- [ ] Ao clicar em "Delete" no `AlarmCard`, um modal de confirmação aparece
- [ ] Modal exibe: "Are you sure you want to delete alarm 'X'?" + botões "Cancel" e "Delete"
- [ ] Alarme só é deletado após confirmação
- [ ] Modal pode ser fechado com ESC ou clicando fora
- [ ] Teste unitário cobre o fluxo de cancelamento

---

### Story 5.2 — Error Boundaries no React

**Como** usuário do Broder  
**Quero** que a aplicação não quebre completamente se um componente falhar  
**Para que** eu possa continuar usando outras partes do sistema

**Critérios de Aceite:**
- [ ] `ErrorBoundary` implementado no nível da aplicação (router ou `App.tsx`)
- [ ] Erros em componentes filhos exibem uma mensagem amigável: "Something went wrong. Please reload the page."
- [ ] Erro é logado no console para debug
- [ ] Teste verifica que um componente que lança erro não derruba a aplicação inteira

---

### Story 5.3 — Idioma Consistente (Inglês)

**Como** visitante internacional no LinkedIn  
**Quero** que toda a interface esteja em inglês  
**Para que** o produto pareça pronto para o mercado global

**Critérios de Aceite:**
- [ ] Todas as labels da UI traduzidas para inglês: "Alarms", "History", "Create Alarm", "Builder", etc.
- [ ] Mensagens de erro da API em inglês (ou pelo menos consistentes)
- [ ] README principal em inglês (ou versão bilingue com EN no topo)
- [ ] Documentação interna (AGENTS.md) pode permanecer em PT (é para devs)

---

## Resumo por Prioridade

| # | Story | Épico | Bloqueador? | Estimativa |
|---|-------|-------|-------------|------------|
| 1 | Docker Compose Unificado | 1 | ✅ CRÍTICO | 4h |
| 2 | Repositório Git Limpo | 1 | ✅ CRÍTICO | 1h |
| 3 | README com Identidade | 1 | ✅ CRÍTICO | 3h |
| 4 | Testes Unitários Domínio | 2 | ✅ CRÍTICO | 4h |
| 5 | Teste Integração Alarmes | 2 | ✅ CRÍTICO | 3h |
| 6 | Testes Unitários Frontend | 2 | ✅ CRÍTICO | 3h |
| 7 | Health Checks | 3 | ✅ CRÍTICO | 2h |
| 8 | Webhook Genérico | 4 | ✅ CRÍTICO | 4h |
| 9 | Métricas Micrometer | 3 | 🟡 Should Have | 3h |
| 10 | Profile PostgreSQL | 3 | 🟡 Should Have | 2h |
| 11 | Confirmação de Deleção | 5 | 🟡 Should Have | 2h |
| 12 | Error Boundaries | 5 | 🟡 Should Have | 2h |
| 13 | Idioma Consistente | 5 | 🟡 Should Have | 3h |
| 14 | Webhook Slack (exemplo) | 4 | 🔵 Nice to Have | 2h |

---

## Notas para o Time

1. **Idioma:** O público-alvo do LinkedIn é global. Recomendo inglês para UI pública, mantendo documentação interna (AGENTS.md) em português se preferir.
2. **Webhook:** Definir se será configuração **por alarme** (cada um tem seu webhook) ou **global** (um webhook para todo o sistema). Recomendo **por alarme** para flexibilidade.
3. **Banco de dados:** O SQLite é aceitável para o beta desde que documentado como "dev only". PostgreSQL é should-have, não blocker.
4. **Prazo estimado:** 3–5 dias de trabalho focado para resolver todos os bloqueadores críticos.

---

*Documento criado em: 2026-04-28*
*Próxima revisão: após resolução dos 8 bloqueadores críticos*
