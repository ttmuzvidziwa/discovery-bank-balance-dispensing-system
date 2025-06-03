
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

## 🔎 REST APIs 

| Endpoint                                    | Method | RequestParams                                  | Description                                     |
|---------------------------------------------|--------|------------------------------------------------|-------------------------------------------------|
| `/discovery-atm/queryTransactionalBalances` | GET    | `clientId`                                     | Fetch transactional account balances.           |
| `/discovery-atm/queryForexBalances`         | GET    | `clientId`                                     | Fetch forex account balances.                   |
| `/discovery-atm/withdraw`                   | POST   | `clientId`, `atmId`, `accountNumber`, `amount` | Initiate withdrawal from specified ATM/account. |

## ⚙️ Getting Started
1. Clone the repo:
   ```bash
   git clone https://github.com/ttmuzvidziwa/discovery-bank-balance-dispensing-system.git
   ```
2. Build & Run:
   ```bash
   ./mvnw clean spring-boot:run
   ```
3. Access the API via Swagger UI:  
   [http://localhost:8080/discovery-atm/swagger-ui/index.html](http://localhost:8080/discovery-atm/swagger-ui/index.html)

## 🗄️ H2 Database
- Accessible at [http://localhost:8080/discovery-atm/h2-console](http://localhost:8080/discovery-atm/h2-console)
- Default JDBC URL: `jdbc:h2:mem:bankbalancedispencingdb`
- Database uses the typical H2 username & password combination

## 🧪 Testing
- Run tests:
   ```bash
   ./mvnw test
   ```
- Contains unit & integration tests covering service and controller layers.
- Code coverage require currently set at 60%.

## 💡 Usage Flow
1. Start in your favourite browser and navigate to `http://localhost:8080/discovery-atm/` 
2. Load a client profile using the **client ID input**.
3. Choose **transactional balances**, **forex balances**, or **withdraw**.
4. View results or proceed with withdrawal, navigating with intuitive buttons.
5. Logout to load a new client profile. 

## 📝 TODO
- [ ] Enhance **error handling and validation**.
- [ ] Implement **security `(out-of-scope, for now)`**.
- [ ] Increase test code coverage threshold to **80%**.

## 👤 Author
Tao Muzvidziwa – *Developer*

---

*Powered by Spring Boot, fueled by coffee ☕*