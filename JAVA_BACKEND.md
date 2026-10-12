# TransitOps — Java Spring Boot Backend

This directory contains the production-ready Java port of the TransitOps backend, built using **Spring Boot 3.2.4** and **Java 17**. It delivers 100% feature and API parity with the Node.js Express backend, matching all routes, JWT authentication, RBAC authorization, JSON payload structures, and database schema.

---

## 🏗️ Architecture & Technology Stack

| Component | Technology | Description |
|---|---|---|
| **Framework** | Spring Boot 3.2.4 | Modern RESTful Web API |
| **Java Version** | Java 17 LTS | Records, Pattern Matching, Modern Standard Library |
| **Persistence** | Spring Data JPA + Hibernate | PostgreSQL relational mappings & type-safe repositories |
| **Query Engine** | Spring JdbcTemplate | High-performance reporting and aggregation queries |
| **Security** | Spring Security 6 + JJWT 0.12.5 | Stateless JWT authentication, BCrypt(12) hashing, method-level RBAC (`@PreAuthorize`) |
| **Database Pool** | HikariCP | High-throughput connection pooling |
| **Seeding** | CommandLineRunner (`DataSeeder`) | Automatic demo data initialization on startup |

---

## 🚀 Running the Java Backend

### Prerequisites
- **JDK 17** or higher installed (`java -version`)
- **Maven** installed (`mvn -version`)
- **PostgreSQL 14+** running on `localhost:5432` with a database named `transitops`

### Option 1: Quick Run via Batch Script (Windows)
Double-click or run:
```cmd
run-java-backend.bat
```

### Option 2: Running via Maven Command
```bash
cd backend-java
mvn clean spring-boot:run
```

### Option 3: Building a Standalone Executable JAR
```bash
cd backend-java
mvn clean package -DskipTests
java -jar target/transitops-backend-1.0.0.jar
```

The server starts on **http://localhost:5000** (matching frontend `baseURL: http://localhost:5000/api`).

---

## 📡 API Endpoints Parity Table

All endpoints return uniform JSON responses matching the Node.js format: `{ success: boolean, message?: string, data?: any }`.

### 1. Authentication (`/api/auth`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register new user account |
| `POST` | `/api/auth/login` | Public | Authenticate user & return JWT token |
| `GET` | `/api/auth/me` | Authenticated | Get current authenticated user profile |
| `GET` | `/api/auth/users` | `fleet_manager` | List all users (User management) |
| `PUT` | `/api/auth/users/{id}/role` | `fleet_manager` | Update user RBAC role |
| `PUT` | `/api/auth/users/{id}/status` | `fleet_manager` | Activate or deactivate user |
| `PUT` | `/api/auth/profile` | Authenticated | Update user name and email |
| `PUT` | `/api/auth/change-password` | Authenticated | Change current password |

### 2. Fleet & Vehicles (`/api/vehicles`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/vehicles` | Authenticated | List all vehicles with aggregated fuel, maintenance costs, and trip count |
| `GET` | `/api/vehicles/available` | Authenticated | List vehicles currently with status `Available` |
| `GET` | `/api/vehicles/{id}` | Authenticated | Get single vehicle details |
| `POST` | `/api/vehicles` | `fleet_manager` | Register a new vehicle |
| `PUT` | `/api/vehicles/{id}` | `fleet_manager` | Update vehicle specifications |
| `DELETE` | `/api/vehicles/{id}` | `fleet_manager` | Delete a vehicle (blocked if `On Trip`) |

### 3. Drivers (`/api/drivers`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/drivers` | Authenticated | List drivers with safety scores and license expiration alerts |
| `GET` | `/api/drivers/available` | Authenticated | List available drivers with valid (non-expired) licenses |
| `GET` | `/api/drivers/{id}` | Authenticated | Get driver details |
| `POST` | `/api/drivers` | Authenticated | Add a new driver |
| `PUT` | `/api/drivers/{id}` | Authenticated | Update driver details |
| `DELETE` | `/api/drivers/{id}` | Authenticated | Delete a driver (blocked if `On Trip`) |

### 4. Trip Dispatch & Lifecycle (`/api/trips`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/trips` | Authenticated | List trips with driver, vehicle, and creator details |
| `GET` | `/api/trips/{id}` | Authenticated | Get trip details |
| `POST` | `/api/trips` | Authenticated | Create a draft trip (validates vehicle capacity & driver license) |
| `POST` | `/api/trips/{id}/dispatch` | Authenticated | Transition trip to `Dispatched`; vehicle & driver set to `On Trip` |
| `POST` | `/api/trips/{id}/complete` | Authenticated | Complete trip; record actual distance/fuel; vehicle & driver restored to `Available` |
| `POST` | `/api/trips/{id}/cancel` | Authenticated | Cancel trip; restores vehicle and driver availability |

### 5. Maintenance (`/api/maintenance`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/maintenance` | Authenticated | List maintenance records with vehicle details |
| `POST` | `/api/maintenance` | Authenticated | Create log; automatically moves vehicle to `In Shop` |
| `PUT` | `/api/maintenance/{id}` | Authenticated | Update maintenance log details |
| `POST` | `/api/maintenance/{id}/close` | Authenticated | Close maintenance log; vehicle restored to `Available` |

### 6. Fuel & Operational Expenses (`/api/fuel`, `/api/expenses`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/fuel` | Authenticated | List fuel logs with vehicle and trip details |
| `POST` | `/api/fuel` | Authenticated | Record fuel refueling entry |
| `DELETE` | `/api/fuel/{id}` | Authenticated | Delete a fuel log entry |
| `GET` | `/api/expenses` | Authenticated | List miscellaneous expenses (Toll, Insurance, Maintenance, Other) |
| `POST` | `/api/expenses` | Authenticated | Record an expense entry |

### 7. Analytics & Reports (`/api/reports`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/reports/dashboard-kpis` | Authenticated | High-level metrics: vehicle status counts, trip counts, active drivers, fleet utilization % |
| `GET` | `/api/reports/fuel-efficiency` | Authenticated | Fuel consumption vs distance analysis (`km_per_liter`) per vehicle |
| `GET` | `/api/reports/operational-cost` | Authenticated | Total cost breakdown (Fuel + Maintenance + Expenses) per vehicle |
| `GET` | `/api/reports/vehicle-roi` | Authenticated | Return on Investment (Revenue - Operational Costs) / Acquisition Cost % |
| `GET` | `/api/reports/trip-trends` | Authenticated | 6-month historical monthly breakdown of trips, completion rate, and revenue |

### 8. System Health
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/health` | Public | Health status check (`success: true, message: "TransitOps API is running"`) |

---

## 👥 Demo Accounts (Auto-Seeded)

When the backend starts up against a fresh database, `DataSeeder` automatically populates the following accounts:

| Role | Email | Password |
|---|---|---|
| **Fleet Manager** | `fleet@transitops.com` | `password123` |
| **Dispatcher** | `driver@transitops.com` | `password123` |
| **Safety Officer** | `safety@transitops.com` | `password123` |
| **Financial Analyst** | `finance@transitops.com` | `password123` |

---

## 🔧 Environment Configuration

Configurable via environment variables or in `src/main/resources/application.properties`:

| Variable | Default Value | Description |
|---|---|---|
| `PORT` | `5000` | Port for the REST API server |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `transitops` | Database name |
| `DB_USER` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | `transitops_super_secret_jwt_key_2024` | HMAC secret for JWT signature verification |
| `JWT_EXPIRES_MS`| `604800000` (7 days) | Token expiration duration |
| `FRONTEND_URL` | `http://localhost:5173` | Frontend URL for CORS |
| `GOOGLE_CLIENT_ID` | `dummy-client-id` | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | `dummy-client-secret` | Google OAuth2 client secret |
