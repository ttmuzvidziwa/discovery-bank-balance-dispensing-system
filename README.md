
# 💳 Bank Balance and Dispensing System

## 📚 Overview
This project implements a bank ATM simulation backend system designed to:
- Retrieve **transactional and forex account balances**.
- Support **cash withdrawals** with denomination breakdowns.
- Handle **real-time reporting** via SQL scripts.
- Expose a **REST API** documented with Swagger/OpenAPI.

## 🚀 Features
- **Transactional and Forex Balance Retrieval** – Query accounts by client ID.
- **Withdrawals with Denomination Breakdown** – Allocate cash based on ATM inventory.
- **Scheduled Reports** – Generate reports showing account balances (SQL-driven).
- **Swagger UI Integration** – Easy exploration of API endpoints.
- **In-Memory H2 Database** – For testing and simulation.

## 🔨 Tech Stack
- **Java 17+**
- **Spring Boot 3.4.6**
- **Spring Data JPA**
- **H2 Database**
- **Swagger/OpenAPI**
- **JUnit 5 & Mockito** – Unit and integration testing.

## 📑 API Endpoints
| Endpoint                                   | Method | Description                                       |
|---------------------------------------------|--------|---------------------------------------------------|
| `/discovery-atm/queryTransactionalBalances`| GET    | Fetch transactional account balances.             |
| `/discovery-atm/queryForexBalances`        | GET    | Fetch forex account balances.                     |
| `/discovery-atm/withdraw`                  | POST   | Initiate withdrawal from specified ATM/account.   |

## 🔎 Getting Started
1. Clone the repo:
   ```bash
   git clone <repo-url>
   cd <project-dir>
   ```
2. Build & Run:
   ```bash
   ./mvnw clean spring-boot:run
   ```
3. Access the API via Swagger UI:  
   [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## 🗄️ H2 Database
- Accessible at [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- Default JDBC URL: `jdbc:h2:mem:testdb`

## 🧪 Testing
- Run tests:
   ```bash
   ./mvnw test
   ```
- Contains unit & integration tests covering service and controller layers.

## 💡 Usage Flow
1. Start at `/index` with **client ID input**.
2. Choose **transactional balances**, **forex balances**, or **withdraw**.
3. View results or proceed with withdrawal, navigating with intuitive buttons.

## 📝 TODO
- [ ] Integrate **frontend templates (Thymeleaf)**.
- [ ] Enhance **error handling and validation**.
- [ ] Implement **security (optional)**.

## 👤 Author
Tao Muzvidziwa – *Developer & Technical Architect*

---

*Powered by Spring Boot, fueled by coffee ☕*