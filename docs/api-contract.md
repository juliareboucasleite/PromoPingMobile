# Contrato API e mapeamento BD – PromoPing (app móvel)

A API e a base de dados de produção são **PostgreSQL** (`papv5`, porta 5432).
Os identificadores na BD estão em minúsculas (`referenciaid`, `precoatual`); o backend devolve JSON com os nomes abaixo.

## 1. Base URL e porta
- Dev: http://<IP_DA_MAQUINA>:3000 ou http://localhost:3000 (emulador Android: http://10.0.2.2:3000)
- Prod: mesmo domínio do site (ex.: https://api.promoping.pt ou BASE_URL/API_URL do .env)
- Porta da API: 3000 (PORT). Porta da BD: 5432.

## 2. Autenticação (JWT)
- Header em todas as rotas: Authorization: Bearer <access_token_jwt>
- Token é o access token devolvido no login (POST /api/auth/login ou OAuth)
- Respostas: 401 (faltando/expirado), 403 (inválido)

## 3. Perfil e preferências
### 3.1 GET /api/user/profile (auth)
Resposta 200:
{
  "status": "ok",
  "profile": {
    "nome": "string",
    "email": "string",
    "telefone": "string|null",
    "FotoPerfil": "string|null",
    "perfilId": 1,
    "contas_conectadas": [{"Tipo": "email|telefone|discord", "Conectado": 0|1}],
    "preferencias": [{"Tipo": "string", "Ativo": 0|1}],
    "proxima_alteracao_senha": "ISO|null",
    "proxima_alteracao_nome": null,
    "pode_alterar_senha": true|false,
    "pode_alterar_nome": true
  }
}
Mapping app: preferencias Tipo "email" -> notificacoesEmail; "discord" -> notificacoesDiscord (Ativo 1/0 -> true/false).
Colunas BD: `utilizadores.nome`, `email`, `telefone`, `fotoperfil`, `perfilid`, `ultimaalteracaosenha`.

### 3.2 PUT /api/user/profile (auth)
Body (opcionais): {
  "nome": "string",
  "email": "string",
  "telefone": "string",
  "fotoPerfil": "string",
  "photo_url": "string"
}
Nota: nome já não tem cooldown; senha tem cooldown 30 dias (`ultimaalteracaosenha`).
Resp 200: {"status":"ok","message":"Perfil atualizado com sucesso"}
Obs: preferências não mudam aqui; usar /api/user/preferences.

### 3.3 Preferências de notificação
GET /api/user/preferences (auth)
Resp: {"status":"ok","preferences":[{"Tipo":"email","Ativo":1},{"Tipo":"discord","Ativo":0}]}
Mapping: Tipo email/discord -> notificacoesEmail/Discord; Ativo 1/0 -> true/false.
Tabela: `preferenciasnotificacao` (`tipo` varchar, `ativo` integer 0/1).

PUT /api/user/preferences (auth)
Body:
{
  "preferences": [
    {"tipo": "email", "ativo": true},
    {"tipo": "discord", "ativo": false}
  ]
}
Resp 200: {"status":"ok"}

## 4. Produtos (todas com auth)
### 4.1 GET /api/produtos
Resp 200:
{
  "status": "ok",
  "produtos": [
    {
      "Id": 1,
      "Nome": "string",
      "Link": "string",
      "PrecoAtual": 19.99,
      "PrecoAlvo": 15.0,
      "DataCriacao": "timestamp",
      "DataLimite": "date|null",
      "Loja": "string",
      "storeInfo": {"name":"string", ...},
      "Historico": [{"Preco":19.99,"Data":"..."}]
    }
  ]
}

### 4.2 POST /api/produtos
Body:
{
  "nome": "string",
  "link": "string (http/https)",
  "data": "date|null",
  "precoAlvo": number
}
Obrigatórios: nome, link, precoAlvo>0.
Resp 200: {"status":"ok","message":"...","produto":{...},"storeInfo":...,"comparisonAvailable":bool}
Erros: 400 campos/URL, 403 limite plano.

### 4.3 PUT /api/produtos/:id
Body (opcionais): {"nome":..., "link":..., "data":...}
Resp 200: {"status":"ok","message":"Produto atualizado com sucesso"}
404 se não existir/pertencer.

### 4.4 DELETE /api/produtos/:id
Resp 200: {"status":"ok","message":"Produto removido com sucesso"}
404 se não encontrado.

### 4.5 GET /api/produtos/:id/historico
Resp 200: {"status":"ok","historico":[{"preco":...,"data":"..."}]}

## 5. Mapeamento PostgreSQL (produção `papv5`)
Schema alinhado com o servidor. Colunas na BD são minúsculas; o backend faz lookup case-insensitive.

### 5.1 utilizadores
PK: `referenciaid` varchar(13).
Colunas: referenciaid, nome, email, senhahash, telefone, codigotelefone, ativo (int 0/1), datadesativacao, dataregisto, ultimologin, perfilid, emailverificado, codigoemail, datanascimento, fotoperfil, createdat, updatedat, discord_id, google_id, ultimo_login, ultimaalteracaosenha, ultimaalteracaonome, dinheiro_poupado.

### 5.2 preferenciasnotificacao
Colunas: id (PK), referenciaid (FK utilizadores), tipo (varchar50: email|discord), ativo (integer 0/1). UNIQUE (referenciaid, tipo).
Mapping app: notificacoesEmail -> tipo email, notificacoesDiscord -> tipo discord.

### 5.3 produtos
Colunas: id (PK), referenciaid (FK utilizadores), nome, link, precoatual, precoalvo, datalimite, shipping, lojaid (FK lojas), createdat, updatedat, deletedat, loja.
Entrada: nome->nome, link->link, precoAlvo->precoalvo, data->datalimite. lojaid setado pelo backend (detecção por link). Saída inclui Loja via join com `lojas.nome`.

### 5.4 historicoprecos
Colunas: id (PK), produtoid (FK produtos), preco, dataregisto, precoanterior, loja, status (default 'Ativo'), observacoes, updatedat.
GET /produtos usa Preco + DataRegisto no array Historico.

### 5.5 outras
- lojas: id, nome, dominio, cssselectorpreco, createdat
- contasconectadas: id, referenciaid, tipo, identificador, conectado, dataconexao
- configutilizador: referenciaid, planoatualid, planoativoid, limiteprodutos, historicodias, statusassinatura, ...
- planos: id, nome, preco, limiteprodutos, historicodias, intervaloverificacao, permitesms, relatorios, linksplanos, linksplanosanual, precoanual
- notificacoes: referenciaid, produtoid, tipo, mensagem, enviada, valorpoupado, dataenvio
- qr_tokens: code, session_id, referenciaid, email, status, expires_at, used_at, created_at, token, refresh_token
- user_sessions: session_id, referenciaid, refresh_token_hash, user_agent, ip_address, browser, platform, device_label, created_at, last_seen_at, revoked_at

## 6. Credenciais e testes
- Criar user via site ou POST /api/auth/register, depois login para obter token.
- Exemplo login:
  curl -X POST http://localhost:3000/api/auth/login -H "Content-Type: application/json" -d '{"email":"teu@email.com","password":"tua_senha"}'
- Usar token:
  curl -X GET http://localhost:3000/api/user/profile -H "Authorization: Bearer SEU_JWT"
  curl -X GET http://localhost:3000/api/produtos -H "Authorization: Bearer SEU_JWT"

## 7. Arquivos backend (referência)
- backend/server.js (rotas /api/user, /api/produtos)
- backend/middleware/auth.js (Authorization: Bearer)
- backend/routes/user.js (GET/PUT /profile, GET /me)
- backend/routes/preferences.js (GET/PUT /api/user/preferences)
- backend/routes/produtos.js (CRUD + historico)
- sql/PAPv5.postgres.sql (schema PostgreSQL de produção)
- backend/database/db.js (pool pg + compat mysql2)
- scripts/migrate-db-postgres.js (colunas/tabelas extra)
