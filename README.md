# 🚀 Sistema Integrado de Controle de Acesso (Monorepo)

Este repositório consolida um sistema completo de controle de acesso, estruturado como um Monorepo composto por três módulos essenciais: um software de cadastro (Frontend), uma API de gestão de dados (Backend) e uma aplicação móvel. O projeto visa demonstrar a integração de tecnologias em múltiplas camadas de desenvolvimento.

---

## 🛠️ Arquitetura e Componentes do Sistema

O sistema opera em uma arquitetura de três camadas interconectadas:

| Módulo | Tipo | Função Primária | Localização |
| :--- | :--- | :--- | :--- |
| **Cadastro** | Frontend (Web/Desktop) | Interface principal para o registro de placas de veículos e gerenciamento de dados primários. | `/cadastro-outro-projeto` |
| **API de Serviços** | Backend/Gateway | Atua como a ponte de comunicação, processando requisições do App Mobile e interagindo diretamente com o Banco de Dados. | `/api-outro-projeto` |
| **App Mobile** | Frontend (React Native) | Permite a consulta remota e a conexão ao sistema central, facilitando o acesso ao banco de dados através da API. | `/app` (ou `src/`) |

---

## ⚙️ Requisitos de Ambiente (Pré-requisitos)

Para garantir o funcionamento correto de todos os módulos, o ambiente de execução deve satisfazer os seguintes requisitos:

### 1. Banco de Dados e Conectividade

| Requisito | Detalhe Técnico | Configuração Necessária |
| :--- | :--- | :--- |
| **Servidor** | SQL Server (Versão 2017+ recomendada) | O banco deve estar acessível na rede (local ou remota). |
| **Driver** | Microsoft JDBC Driver para SQL Server (v12.4 ou superior). | Versão compatível com JDK 21+. |
| **Protocolo** | Conexão TCP/IP. | O serviço do SQL Server deve ter o **TCP/IP habilitado** na **porta padrão 1433**. |
| **Firewall** | Regra de entrada. | O Firewall do Windows (na máquina host do SQL Server) deve permitir o tráfego de entrada na porta **1433**. |

### 2. Ambiente de Desenvolvimento

* **Java Development Kit (JDK):** Versão **21 ou superior**.
* **Node.js:** Versão 18.x ou 20.x (para o App Mobile).
* **Expo CLI:** (Recomendado para o desenvolvimento do App Mobile).

---

## 💻 Configuração e Execução

### 1. Configuração da API (Backend)

O arquivo de configuração da API (geralmente `application.properties` ou similar) deve ser ajustado para incluir a string de conexão do banco de dados:

```properties
# CONEXÃO PORTÁVEL
spring.datasource.url=jdbc:sqlserver://${DB_HOST:localhost}:${DB_PORT:1433};databaseName=${DB_NAME:EditPro};trustServerCertificate=${DB_TRUST_CERT:true}
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.datasource.username=${DB_USER:sa}
spring.datasource.password=${DB_PASS:tester}

# JPA - CORREÇÃO PARA SQL SERVER
spring.jpa.hibernate.ddl-auto=${JPA_DDL_AUTO:none}
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect
spring.jpa.properties.hibernate.physical_naming_strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
spring.jpa.properties.hibernate.globally_quoted_identifiers=false

# SERVIDOR
server.port=${SERVER_PORT:8080}
server.address=0.0.0.0

# LOGS
logging.level.com.api.apitest=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
