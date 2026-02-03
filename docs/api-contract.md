# Contrato API e mapeamento BD – PromoPing (app móvel)

## 1. Base URL e porta
- Dev: http://<IP_DA_MAQUINA>:3000 ou http://localhost:3000 (emulador Android: http://10.0.2.2:3000)
- Prod: mesmo domínio do site (ex.: https://api.promoping.pt ou BASE_URL/API_URL do .env)
- Porta padrão: 3000 (PORT)

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
    "contas_conectadas": [{"Tipo": "email|telefone|discord", "Conectado": 0|1}],
    "preferencias": [{"Tipo": "string", "Ativo": 0|1}],
    "proxima_alteracao_senha": "ISO|null",
    "proxima_alteracao_nome": "ISO|null",
    "pode_alterar_senha": true|false,
    "pode_alterar_nome": true|false
  }
}
Mapping app: preferencias Tipo "email" -> notificacoesEmail; "discord" -> notificacoesDiscord (Ativo 1/0 -> true/false).

### 3.2 PUT /api/user/profile (auth)
Body (opcionais): {
  "nome": "string",
  "email": "string",
  "telefone": "string",
  "fotoPerfil": "string",
  "photo_url": "string"
}
Nota: nome tem cooldown 30 dias; erro 400 com proxima_alteracao.
Resp 200: {"status":"ok","message":"Perfil atualizado com sucesso"}
Obs: preferências não mudam aqui; usar /api/user/preferences.

### 3.3 Preferências de notificação
GET /api/user/preferences (auth)
Resp: {"status":"ok","preferences":[{"Tipo":"email","Ativo":1},{"Tipo":"discord","Ativo":0}]}
Mapping: Tipo email/discord -> notificacoesEmail/Discord; Ativo 1/0 -> true/false.

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
Resp 200: {"status":"ok","historico":[{"preco":...,"data":...}]}

## 5. Mapeamento MySQL/MariaDB
### 5.1 utilizadores
PK: ReferenciaID (varchar13). Colunas: ReferenciaID, Nome, Email, Telefone, FotoPerfil, DataRegisto, UltimaAlteracaoSenha, UltimaAlteracaoNome, PerfilId, etc.

### 5.2 preferenciasnotificacao
Colunas: Id (PK), ReferenciaID (FK utilizadores), Tipo (varchar50: email|discord), Ativo (tinyint 0/1). UNIQUE (ReferenciaID, Tipo). Mapping app: notificacoesEmail -> Tipo email, notificacoesDiscord -> Tipo discord.

### 5.3 produtos
Colunas: Id (PK), ReferenciaID (FK utilizadores), Nome, Link, PrecoAtual, PrecoAlvo, DataLimite, Shipping, LojaId (FK lojas), CreatedAt, UpdatedAt, DeletedAt.
Entrada: nome->Nome, link->Link, precoAlvo->PrecoAlvo, data->DataLimite. LojaId setado pelo backend (detecção por link). Saída inclui Loja via join.

### 5.4 historicoprecos
Colunas: Id (PK), ProdutoId (FK produtos), Preco, DataRegisto.

### 5.5 outras
- lojas: Id, Nome, Dominio (produtos usam LojaId)
- contasconectadas: ReferenciaID, Tipo, Identificador, Conectado
- configutilizador: ReferenciaID, LimiteProdutos etc.

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
- backend/database/tableManager.js (CREATE TABLE utilizadores, produtos, preferenciasnotificacao, historicoprecos...)
