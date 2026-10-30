# 🏦 Plateforme Bancaire Distribuée Microservices & Assistant IA

<div align="center">

[![Java 17](https://img.shields.io/badge/Java-17%20%7C%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.16-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2025.0.1-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud)
[![Spring AI](https://img.shields.io/badge/Spring_AI-1.1.8-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-ai)
[![React](https://img.shields.io/badge/React-19.0-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-v4-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-9092-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Python](https://img.shields.io/badge/Python_ML-Scikit_Learn-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://scikit-learn.org/)

<p align="center">
  <strong>Système d'information bancaire moderne haute disponibilité : Microservices événementiels, triple exposition (REST / GraphQL / MCP), tolérance aux pannes, assistant conversationnel autonome et détection de fraude.</strong>
</p>

*Projet d'épreuve technique d'excellence (ADRIA / ENSET) — Architecture microservices modulaire en Maven multi-module (`com.bank`).*

</div>

---

## 📑 Sommaire

1. [Vue d'ensemble & Contexte Métier](#-vue-densemble--contexte-métier)
2. [Architecture Globale du Système](#-architecture-globale-du-système)
3. [Cartographie des Microservices & Ports](#-cartographie-des-microservices--ports)
4. [Règles Métier Bancaires & Validation](#-règles-métier-bancaires--validation)
5. [Architecture en Couches & Triple Exposition](#-architecture-en-couches--triple-exposition-rest--graphql--mcp)
6. [Communication Inter-Services & Tolérance aux Pannes](#-communication-inter-services--tolérance-aux-pannes)
7. [Événements Asynchrones avec Apache Kafka](#-événements-asynchrones-avec-apache-kafka)
8. [Assistant Bancaire IA (Spring AI & MCP)](#-assistant-bancaire-ia-spring-ai--mcp)
9. [Détection de Fraude Financière (Machine Learning)](#-détection-de-fraude-financière-machine-learning)
10. [Client Web (React 19 & shadcn/ui)](#-client-web-react-19--shadcnui)
11. [Arborescence du Répertoire](#-arborescence-du-répertoire)
12. [Guide de Démarrage Rapide](#-guide-de-démarrage-rapide)
13. [Tests & Démonstration](#-tests--démonstration)
14. [DevOps, Observabilité & CI/CD](#-devops-observabilité--cicd)

---

## 🌟 Vue d'ensemble & Contexte Métier

Cette plateforme bancaire modulaire répond aux exigences critiques des systèmes transactionnels financiers modernes :
- **Gestion des comptes & conformité financière** : création, consultation, blocage de sécurité et opérations de crédit/débit avec normalisation systématique des calculs monétaires (`BigDecimal`, arrondi `HALF_UP` à 2 décimales).
- **Moteur de transactions & virements atomiques** : vérification stricte de plafonds dynamiques, identité de devise, vérification de solde et virement compte-à-compte avec compensation automatique en cas d'incident.
- **Triple exposition protocolaire unifiée** : chaque service métier expose ses fonctionnalités via **REST (OpenAPI/Swagger)**, **Spring for GraphQL** et des **outils MCP (Model Context Protocol)** branchés directement sur une même couche métier sans duplication.
- **Communication hybride** : requêtes synchrones inter-services résilientes via **OpenFeign + Resilience4J** (disjoncteur / fallback explicite HTTP 503) et publication asynchrone d'événements financiers via **Apache Kafka**.
- **IA Agentique intégrée** : un assistant conversationnel propulsé par **Spring AI** utilise les outils MCP des microservices pour répondre aux clients, consulter les soldes et initier des transactions sécurisées.
- **Piste expérimentale de détection de fraude** : un pipeline Python / scikit-learn robuste entraîné sur un jeu de données déséquilibré pour identifier en temps réel les transactions suspectes.

---

## 🏗️ Architecture Globale du Système

Le schéma ci-dessous illustre l'interaction entre les clients, la périphérie d'accès (Edge Gateway), les serveurs de découverte/configuration, les microservices métier, le broker Kafka et l'agent d'intelligence artificielle :

```mermaid
flowchart LR
  subgraph CLIENTS["🌐 Clients & Canaux"]
    UI["💻 React Web UI<br/>(Vite :5173 / Docker :3000)"]
    TG["📱 Telegram Bot"]
    DC["🎮 Discord Bot"]
    RC["⚡ Client REST / Postman / curl"]
  end

  subgraph EDGE["🛡️ Périphérie & Passerelle"]
    GW["gateway-service<br/>Spring Cloud Gateway (:8080)"]
  end

  subgraph INFRA["⚙️ Gouvernance & Découverte"]
    EU["discovery-service<br/>Netflix Eureka (:8761)"]
    CS["config-service<br/>Spring Cloud Config (:8888)<br/>bank.transaction.max-amount=1000"]
    KC["Keycloak (Optionnel :8180)<br/>Authentification JWT / OAuth2"]
  end

  subgraph SERVICES["💼 Microservices Métier"]
    ACC["account-service (:8081)<br/>REST + GraphQL + MCP<br/>Stockage JPA / H2"]
    TRX["transaction-service (:8082)<br/>REST + GraphQL + MCP<br/>Stockage JPA / H2"]
    AST["assistant-service (:8083)<br/>Spring AI + Orchestration LLM"]
  end

  subgraph ASYNC["📨 Événements Asynchrones"]
    KAFKA[["Apache Kafka (:9092)<br/>Topic: bank.transactions.created<br/>Clé de partition: accountId"]]
    EVC["event-consumer-service (:8084)<br/>Audit & Ingestion (Lecture seule)"]
  end

  LLM["🤖 Modèle LLM<br/>(OpenAI / Groq / Anthropic / Ollama)"]

  %% Liaisons clients -> Gateway
  UI -->|HTTP / JSON| GW
  TG -->|Webhook / Polling| GW
  DC -->|API Gateway| GW
  RC -->|Appels directs| GW

  %% Découverte & Configuration
  GW -.->|Résolution dynamique| EU
  ACC -.->|Enregistrement Eureka| EU
  TRX -.->|Enregistrement Eureka| EU
  AST -.->|Enregistrement Eureka| EU
  EVC -.->|Enregistrement Eureka| EU
  CS -.->|Bootstrap config| ACC
  CS -.->|Bootstrap config| TRX
  GW -.->|Validation JWT| KC

  %% Routage Gateway vers services
  GW -->|/api/accounts/**| ACC
  GW -->|/api/transactions/**| TRX
  GW -->|/api/chat/**| AST

  %% Synchronisation inter-services
  TRX ==>|OpenFeign + Resilience4J<br/>Vérification & Débit solde| ACC
  AST ==>|MCP Client (Tools)| ACC
  AST ==>|MCP Client (Tools)| TRX
  AST <-->|Prompt + Function Calling| LLM

  %% Flux asynchrone Kafka
  TRX ==>|Événement TransactionCreated<br/>AFTER_COMMIT (Clé accountId)| KAFKA
  KAFKA ==>|Consommation temps réel| EVC
```

---

## 🔌 Cartographie des Microservices & Ports

Tous les microservices Java partagent le socle technique **Spring Boot 3.5.16** + **Spring Cloud 2025.0.1** sous le groupe Maven `com.bank`.

| Service | Port | Rôle & Responsabilité | Protocoles & Interfaces | Documentation & Santé |
|:---|:---:|:---|:---|:---|
| **`discovery-service`** | `8761` | Annuaire et enregistrement dynamique des instances (Eureka Server). | HTTP / Dashboard Eureka | [http://localhost:8761](http://localhost:8761) |
| **`config-service`** | `8888` | Gestion centralisée des configurations externalisées (profil `native`, dossier `config-repo/`). | HTTP Config Server | `/actuator/health`, `/{service}/default` |
| **`gateway-service`** | `8080` | Passerelle applicative unique, équilibrage de charge, routage dynamique et sécurité. | HTTP / WebFlux | `/actuator/health` |
| **`account-service`** | `8081` | Gestion du cycle de vie des comptes bancaires, soldes et statuts. | REST, GraphQL, MCP, H2 Console | `/swagger-ui.html`, `/graphql`, `/graphiql` |
| **`transaction-service`** | `8082` | Gestion des dépôts, retraits, règles de plafond, virements et publication Kafka. | REST, GraphQL, MCP, OpenFeign, H2 | `/swagger-ui.html`, `/graphql` |
| **`assistant-service`** | `8083` | Assistant conversationnel bancaire propulsé par Spring AI et outillage MCP. | REST (`/api/chat`), Telegram, Discord | `/actuator/health` |
| **`event-consumer-service`** | `8084` | Consommateur d'événements Kafka dédié à l'audit et à la journalisation (lecture seule). | Kafka Consumer Listener | `/actuator/health` |
| **`frontend`** | `5173` / `3000` | Application web réactive React 19 + TypeScript + shadcn/ui. | SPA (Vite / Nginx) | [http://localhost:5173](http://localhost:5173) |
| **`Apache Kafka`** | `9092` | Broker de messages d'événements bancaires (mode KRaft). | Protocole binaire Kafka | Topic `bank.transactions.created` |
| **`Keycloak`** *(opt.)* | `8180` | Serveur d'identité et gestion des jetons JWT. | OAuth2 / OIDC | [http://localhost:8180](http://localhost:8180) |

---

## ⚖️ Règles Métier Bancaires & Validation

Le système applique des contraintes strictes d'intégrité transactionnelle conformément aux exigences bancaires :

### Modèles Minimaux

```
Account:
  - id: UUID (Clé primaire)
  - customerReference: String (Unique, e.g. "ACC-001")
  - status: ACTIVE | BLOCKED
  - currency: String (ISO 4217, e.g. "EUR", "MAD")
  - balance: double (Normalisé en BigDecimal(2, HALF_UP))

Transaction:
  - id: UUID
  - accountId: UUID
  - amount: BigDecimal (Montant positif)
  - currency: String (ISO 4217)
  - merchantCategory: String
  - country: String
  - createdAt: Instant
  - type: DEBIT | CREDIT
  - transferId: UUID (Nullable, commun aux deux jambes d'un virement)
```

### Règles d'Exécution d'une Transaction (Ordonnancement Strict)

1. **Validation du montant (R1)** : `amount` strictement supérieur à 0.
2. **Plafond dynamique (R2)** : `amount` $\le$ `bank.transaction.max-amount` (valeur par défaut : `1000`, servie dynamiquement par le `config-service`).
3. **Existence du compte (R3)** : interrogation synchrone via `AccountClient` (OpenFeign) ; renvoie une erreur `404 Not Found` si le compte n'existe pas.
4. **État du compte (R4)** : le compte doit être au statut `ACTIVE` ; si le compte est `BLOCKED`, l'opération est immédiatement rejetée (`409 Conflict`).
5. **Concordance de devise (R5)** : la devise de la transaction doit être strictement identique à celle du compte concerné (`422 Unprocessable Entity`).
6. **Contrôle de provision (R6)** : pour un débit, le solde disponible doit être supérieur ou égal au montant demandé (`422 Unprocessable Entity`).
7. **Persistance locale (R7)** : enregistrement de la transaction en base de données.
8. **Mise à jour du solde (R8)** : appel synchrone à `account-service` via Feign (`PATCH /api/accounts/{id}/balance`) pour appliquer la variation.
   - *Atomicité garantie* : si la mise à jour du solde échoue, la transaction est annulée par rollback `@Transactional` (aucune transaction orpheline).
9. **Virement entre comptes** :
   - Application simultanée des règles sur les deux comptes (compte source et compte destinataire actifs et de devise identique).
   - Création de deux écritures transactionnelles liées par un `transferId` identique.
   - En cas d'échec sur la seconde jambe, mécanisme de **compensation automatique** avec re-crédit de la source.

---

## 🧩 Architecture en Couches & Triple Exposition (REST / GraphQL / MCP)

Chaque microservice métier respecte rigoureusement une séparation en couches découplées :

```
com.bank.<service>/
├── domain/            # Entités JPA pures et énumérations métier
├── repository/        # Interfaces Spring Data JPA
├── service/           # Couche métier UNIQUE (AccountService, TransactionService)
│   └── dto/           # Objets de transfert de données et requêtes d'opérations
├── web/
│   ├── rest/          # Contrôleurs REST, documentation OpenAPI / Swagger
│   └── graphql/       # Contrôleurs Spring for GraphQL et résolveurs de schéma
├── mcp/               # Adaptateurs Spring AI MCP Server (annotations @Tool)
├── client/            # Clients Feign et fabriques de fallback Resilience4J
├── event/             # Producteurs d'événements Kafka (@TransactionalEventListener)
├── exception/         # Exceptions métier et GlobalExceptionHandler (ProblemDetail RFC 9457)
└── config/            # Initialisation des données de démo et configurations
```

> [!IMPORTANT]
> **Règle d'or architecturale** : Les contrôleurs REST, les résolveurs GraphQL et les serveurs d'outils MCP sont de purs adaptateurs qui délèguent **à la même couche `service/`**. Aucune logique métier n'est dupliquée dans les contrôleurs.

### 1. Exposition RESTful
- Endpoints documentés automatiquement via SpringDoc OpenAPI 2.8.x sur `/swagger-ui.html` et `/v3/api-docs`.
- Gestion centralisée des erreurs avec production de réponses standardisées **ProblemDetail (RFC 9457)**.

### 2. Exposition GraphQL
- Définition de schémas explicites (`schema.graphqls`).
- Console interactive **GraphiQL** accessible pour tester requêtes (`Query`) et mutations (`Mutation`).

### 3. Exposition Model Context Protocol (MCP)
- Utilisation des primitives **Spring AI** (`@Tool`, `@ToolParam`).
- Découverte dynamique des fonctionnalités bancaires par les agents intelligents (assistant autonome).
- Outils disponibles pour les comptes : `listAccounts`, `getAccount`, `getAccountBalance`, `createAccount`, `updateAccountStatus`.

---

## 🛡️ Communication Inter-Services & Tolérance aux Pannes

La communication synchrone entre `transaction-service` et `account-service` est sécurisée par **Spring Cloud OpenFeign** et **Resilience4J** :

```
[ transaction-service ] --- (OpenFeign: AccountClient) ---> [ account-service ]
                                       │
                              (Resilience4J)
                                       ├── Timeout & Retry (erreurs techniques 5xx)
                                       ├── Circuit Breaker (Disjoncteur actif)
                                       └── FallbackFactory (AccountClientFallback)
                                                  │
                                                  ▼
                                      HTTP 503 Service Unavailable
                                   (Rejet propre, AUCUN faux positif)
```

- **Dégradation maîtrisée** : En cas d'indisponibilité d'un service, le disjoncteur bascule en état `OPEN` et le fallback renvoie une erreur explicite **HTTP 503**.
- **Sécurité transactionnelle** : Le fallback ne simule **jamais** une écriture ou une mise à jour fictive de solde en cas de panne.
- **Observabilité** : L'état du circuit breaker est auditable en direct via `/actuator/circuitbreakers` et `/actuator/health`.

---

## 📨 Événements Asynchrones avec Apache Kafka

Après confirmation et validation d'une transaction, le système notifie les consommateurs asynchrones via un événement financier immuable.

### Contrat d'Événement (`TransactionCreatedEvent`)
- **Topic** : `bank.transactions.created`
- **Clé de partition** : `accountId` (garantit le partitionnement et l'ordre chronologique des transactions *par compte*)
- **Phase de déclenchement** : `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` pour garantir qu'aucune transaction annulée ou rollbackée n'est jamais publiée.

```json
{
  "eventId": "a7b8c9d0-1234-5678-90ab-cdef12345678",
  "eventType": "TransactionCreated",
  "transactionId": "b1c2d3e4-5678-90ab-cdef-1234567890ab",
  "accountId": "c2d3e4f5-6789-01bc-def0-2345678901bc",
  "amount": 250.00,
  "currency": "EUR",
  "occurredAt": "2026-10-30T10:30:00Z"
}
```

### Consommateur Dédié (`event-consumer-service`)
- Consommateur `@KafkaListener` sous le groupe `transaction-event-consumer`.
- Rôle strictement en lecture seule : journalisation structurée et audit sans écriture en base.
- Protection contre les *poison messages* via `ErrorHandlingDeserializer` et `DefaultErrorHandler`.

---

## 🤖 Assistant Bancaire IA (Spring AI & MCP)

Le service `assistant-service` (port `8083`) implémente un agent conversationnel bancaire autonome s'appuyant sur l'architecture **Model Context Protocol (MCP)** :

```
                  ┌──────────────────────────────────────────────┐
                  │              assistant-service               │
                  │  (ChatClient + MessageChatMemoryAdvisor)     │
                  └──────────────────────┬───────────────────────┘
                                         │
                   Découverte dynamique des outils (@Tool)
                                         │
                 ┌───────────────────────┴───────────────────────┐
                 ▼                                               ▼
    [ account-mcp-server (:8081) ]                [ transaction-mcp-server (:8082) ]
     - listAccounts                                - getTransactionHistory
     - getAccount                                  - createTransaction
     - getAccountBalance                           - transferFunds
     - createAccount
     - updateAccountStatus
```

- **Principe d'indépendance** : L'assistant n'accède jamais directement aux bases de données H2/JPA ; il interagit exclusivement via les serveurs MCP.
- **Canaux multiples** : Accessible via une interface web (REST `/api/chat`), un bot Telegram et un bot Discord.
- **Règles de sécurité de l'assistant** :
  - Confirmation explicite requise avant toute action modificatrice (virement, blocage, débit).
  - Explication humaine et transparente des refus métier (plafond dépassé, solde insuffisant, compte bloqué).

---

## 📊 Détection de Fraude Financière (Machine Learning)

Situé dans le dossier `ml/`, ce composant expérimental implémente un pipeline complet scikit-learn pour détecter en temps réel les transactions bancaires frauduleuses :

- **Jeu de données réaliste & déséquilibré** : Ratio de fraude représentatif (~2%).
- **Variables explicatives** : `amount`, `hour`, `country`, `merchant_category`, `transactions_last_24h`, `average_amount_last_7d`.
- **Intégrité méthodologique** :
  - Exclusion stricte des identifiants techniques (`transaction_id`, `account_id`) pour éviter le surapprentissage.
  - Découpage stratifié (*Stratified Train/Test Split*) à 80/20 avec graine aléatoire fixée.
  - Préprocesseur `ColumnTransformer` (médiane numérique, imputation du mode catégoriel, `OneHotEncoder`, `StandardScaler`) ajusté **exclusivement sur l'ensemble d'entraînement**.
- **Modélisation & Évaluation** :
  - Modèle de référence : `LogisticRegression(class_weight="balanced")` et classifieur ensembliste (Random Forest).
  - Métriques d'évaluation métier : Matrice de confusion, Précision, Rappel, F1-Score et courbe Précision-Rappel (*Average Precision*).

---

## 💻 Client Web (React 19 & shadcn/ui)

Développé sous `frontend/`, le client web moderne offre une expérience utilisateur réactive :

- **Stack technique** : React 19, TypeScript (mode strict), Vite, Tailwind CSS v4, composants shadcn/ui (Radix primitives), React Router, TanStack Query, react-hook-form + zod.
- **Design System sémantique** : Utilisation exclusive de jetons sémantiques (`bg-background`, `text-foreground`, `border`, `bg-primary`, etc.) supportant nativement les modes clair et sombre.
- **Expérience utilisateur robuste** :
  - Prise en charge des trois états canoniques sur chaque vue : Chargement (`Skeleton`), Vue vide (`EmptyState`) et Erreur (`Alert`).
  - Validation client avec retours visuels précis et propagation des erreurs métier renvoyées par le backend (plafond, provision, compte bloqué).
  - Actions critiques sécurisées par des boîtes de dialogue de confirmation (`AlertDialog`).

---

## 📁 Arborescence du Répertoire

```text
aitsaidouali-saad-test-adria-pfe-27/
├── pom.xml                         # POM parent Maven multi-module (BOMs Spring Boot, Cloud, AI)
├── README.md                       # Documentation d'ingénierie et guide du projet
├── config-repo/                    # Référentiel de configuration centralisé (Config Server)
│   ├── application.yml             # Paramètres partagés (bank.transaction.max-amount=1000)
│   ├── account-service.yml
│   ├── transaction-service.yml
│   └── gateway-service.yml
├── discovery-service/              # Eureka Server (port 8761)
├── config-service/                 # Spring Cloud Config Server native (port 8888)
├── gateway-service/                # Spring Cloud Gateway (port 8080)
├── account-service/                # Microservice des comptes (port 8081, REST + GraphQL + MCP)
│   ├── src/main/java/com/bank/account/
│   │   ├── domain/                 # Entités (Account, AccountStatus)
│   │   ├── repository/             # AccountRepository (Spring Data JPA)
│   │   ├── service/                # AccountService & AccountServiceImpl
│   │   ├── web/                    # REST (OpenAPI) & Spring for GraphQL
│   │   └── mcp/                    # AccountMcpTools (Serveur MCP Spring AI)
│   └── src/main/resources/graphql/ # Schémas GraphQL (account.graphqls)
├── transaction-service/            # Microservice des transactions et virements (port 8082)
├── assistant-service/              # Assistant conversationnel IA (port 8083, Spring AI)
├── event-consumer-service/         # Consommateur d'audit Kafka en lecture seule (port 8084)
├── frontend/                       # Application React 19 + TypeScript + shadcn/ui
├── ml/                             # Détection de fraude en Python (scikit-learn)
├── scripts/                        # Scripts utilitaires d'automatisation et helpers
└── .github/workflows/              # Pipelines d'intégration et déploiement continus (CI/CD)
```

---

## 🚀 Guide de Démarrage Rapide

### Prérequis Système
- **Java SE Development Kit (JDK)** : Version 17 ou 21
- **Apache Maven** : Version 3.9+
- **Node.js** : Version 20+ (pour le frontend)
- **Python** : Version 3.10+ (pour le module ML)
- **Docker & Docker Compose** : Recommandé pour l'orchestration complète

### 1. Compilation Complète du Projet

À la racine du projet, compilez l'ensemble des modules Maven et exécutez les tests unitaires :

```bash
mvn clean verify
```

### 2. Ordre de Démarrage Recommandé (Local)

Pour un fonctionnement optimal, lancez les microservices dans l'ordre suivant dans des terminaux distincts :

#### Étape 1 : Infrastructure de Découverte & Configuration
```bash
# 1. Annuaire Eureka (port 8761)
mvn spring-boot:run -pl discovery-service

# 2. Serveur de Configuration Centralisé (port 8888)
mvn spring-boot:run -pl config-service
```

Vérifiez que la configuration du plafond est bien servie :
```bash
curl -s http://localhost:8888/transaction-service/default | grep max-amount
```

#### Étape 2 : Services Métier
```bash
# 3. Service des Comptes (port 8081)
mvn spring-boot:run -pl account-service

# 4. Service des Transactions (port 8082)
mvn spring-boot:run -pl transaction-service
```

#### Étape 3 : Passerelle & Périphérie
```bash
# 5. Passerelle API Gateway (port 8080)
mvn spring-boot:run -pl gateway-service

# 6. Assistant IA & Consommateur Kafka
mvn spring-boot:run -pl assistant-service
mvn spring-boot:run -pl event-consumer-service
```

#### Étape 4 : Client Web React
```bash
cd frontend
npm install
npm run dev
```
L'interface est accessible sur [http://localhost:5173](http://localhost:5173).

---

## 🧪 Tests & Démonstration

Des données de démonstration sont préchargées automatiquement au démarrage (`DemoDataInitializer`) :
- `ACC-001` : 5 000.00 EUR (Statut `ACTIVE`)
- `ACC-002` : 750.00 EUR (Statut `ACTIVE`)
- `ACC-003` : 10 000.00 MAD (Statut `BLOCKED` pour tests de conformité)

### Exemples d'Appels REST (`curl`)

```bash
# 1. Consulter la liste des comptes existants
curl -X GET http://localhost:8081/api/accounts

# 2. Créer un nouveau compte avec solde initial (normalisation automatique de l'arrondi)
curl -X POST http://localhost:8081/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"customerReference": "ACC-004", "currency": "EUR", "initialBalance": 1250.50}'

# 3. Consulter le solde d'un compte
curl -X GET http://localhost:8081/api/accounts/{id}/balance

# 4. Effectuer une opération de débit nominale
curl -X PATCH http://localhost:8081/api/accounts/{id}/balance \
  -H "Content-Type: application/json" \
  -d '{"operation": "DEBIT", "amount": 200.00}'

# 5. Tester le rejet d'un débit sur un compte bloqué (Attendu : HTTP 409 Conflict)
curl -X PATCH http://localhost:8081/api/accounts/{id-ACC-003}/balance \
  -H "Content-Type: application/json" \
  -d '{"operation": "DEBIT", "amount": 50.00}'
```

### Exemples de Requêtes GraphQL

Accédez à l'interface GraphiQL sur [http://localhost:8081/graphiql](http://localhost:8081/graphiql) :

```graphql
query GetBankAccounts {
  accounts {
    id
    customerReference
    currency
    balance
    status
  }
}

mutation CreateNewAccount {
  createAccount(account: {
    customerReference: "ACC-005",
    currency: "EUR",
    initialBalance: 3000.00
  }) {
    id
    customerReference
    status
    balance
  }
}
```

---

## 📈 DevOps, Observabilité & CI/CD

- **Healthchecks & Probes Kubernetes** : Chaque microservice intègre les sondes d'activité et de préparation Spring Boot Actuator sur `/actuator/health/liveness` et `/actuator/health/readiness`.
- **Conteneurisation Multi-Étapes** : Dockerfile multi-étapes optimisé pour Java 17/21 avec images de base allégées (Alpine/Slim) et exécution non-root sécurisée.
- **Pipeline CI/CD GitHub Actions** :
  - Validation Maven multi-module (`mvn clean verify`).
  - Validation du code TypeScript et tests front-end (Vitest).
  - Build et publication automatique des images Docker étiquetées par SHA de commit.

---

## 👨‍💻 Auteur & Contexte

Projet réalisé dans le cadre de l'épreuve technique d'ingénierie logicielle avancée **ADRIA / ENSET**.

- **Auteur** : Saad Ait Said Ouali
- **Organisation** : ENSET Mohammedia / ADRIA Business & Technology
- **Licence** : Projet académique et technique interne.
