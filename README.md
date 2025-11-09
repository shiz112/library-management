# Library Management

A simple library management Spring Boot application used for demos and learning.
# Library Book Management System (Java Spring Boot)

A web-based system to manage books, issuances/returns, overdue tracking, authentication (librarian/student), reports, and basic backup/restore.

## Prerequisites
- Java 17 (JDK)
- Option A: Apache Maven installed and on PATH
- Option B: Ask to add Maven Wrapper (mvnw) to run without installing Maven

Verify Java:
```bash
java -version
```

If you choose Option A, verify Maven:
```bash
mvn -v
```

## Getting Started (Windows PowerShell)
1. Clone or open the project directory:
   - Project path: `C:\Users\student\Desktop\PBLJ`

2. Configure (optional): change JWT secret in `src/main/resources/application.properties`:
   - `app.jwt.secret=change-this-in-prod-32chars-min-secret-key`

3. Run the application
   - Option A: Using Maven (installed globally)
     ```bash
     cd C:\Users\student\Desktop\PBLJ
     mvn spring-boot:run
     ```
   - Option B: Request Maven Wrapper
     - Ask us to "add wrapper" and then run:
       ```bash
       cd C:\Users\student\Desktop\PBLJ
       .\mvnw spring-boot:run
       ```

4. Access the app
   - API base URL: `http://localhost:8080`
   - H2 Console: `http://localhost:8080/h2-console`
     - JDBC URL: `jdbc:h2:mem:librarydb`
     - Username: `sa`
     - Password: (leave blank)

## Seed Data (default users & books)
- Librarian: `librarian` / `password`
- Student: `student` / `password`
- Sample books: "Clean Code", "Design Patterns"

## Quick API Walkthrough
1) Authenticate to get a JWT token
```bash
# Librarian login
curl -s -X POST http://localhost:8080/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"librarian\",\"password\":\"password\"}"
```
Response:
```json
{"token":"<JWT>"}
```

2) Use the JWT
```bash
# PowerShell: set a variable with your token
$TOKEN = "<paste-token>"

# List books
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/books
```

3) Librarian-only actions
```bash
# Create a book
curl -X POST http://localhost:8080/books ^
  -H "Authorization: Bearer $TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Refactoring\",\"author\":\"Martin Fowler\",\"isbn\":\"9780201485677\",\"category\":\"Software\",\"totalCopies\":2,\"availableCopies\":2}"

# Issue a book to the student (userId usually 2 in seed, adjust as needed)
curl -X POST http://localhost:8080/issuances ^
  -H "Authorization: Bearer $TOKEN" ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":2,\"bookId\":1,\"loanDays\":14}"

# Return a book (issuanceId 1 as example)
curl -X POST http://localhost:8080/issuances/1/return ^
  -H "Authorization: Bearer $TOKEN"
```

4) Reports & Student View
```bash
# Summary report
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/reports/summary

# Student login to view own issuances
$TOKEN_STUDENT = (curl -s -X POST http://localhost:8080/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"student\",\"password\":\"password\"}")

# Use the token value from the response above with /me/issuances
```
```bash
# Example once you paste the student token into $TOKEN_STUDENT
curl -H "Authorization: Bearer <TOKEN_STUDENT>" http://localhost:8080/me/issuances
```

5) Backup / Restore (librarian-only)
```bash
# Export all data to a file
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/backup > backup.json

# Import data from a file (naive merge)
curl -X POST http://localhost:8080/backup ^
  -H "Authorization: Bearer $TOKEN" ^
  -H "Content-Type: application/json" ^
  -d @backup.json
```

## Overdue & Fines
- Due dates are set at issuance (loanDays)
- Fine calculation: 1 unit per overdue day
- Daily scheduler at 08:00 recalculates fines and logs overdue items

## Configuration
Edit `src/main/resources/application.properties`:

## Using MySQL instead of H2

This project runs with an in-memory H2 database by default. To use MySQL (development), follow these steps:

1. Install MySQL server and create a database and user. Example (run in PowerShell or MySQL shell):

```powershell
# in MySQL shell or using mysql client
CREATE DATABASE librarydb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'library_user'@'localhost' IDENTIFIED BY 'your_password_here';
GRANT ALL PRIVILEGES ON librarydb.* TO 'library_user'@'localhost';
FLUSH PRIVILEGES;
```

2. Edit `src/main/resources/application-mysql.properties` and set `spring.datasource.username` and `spring.datasource.password` to your values.

3. Build and run with the `mysql` profile active. From the project root (Windows PowerShell):

```powershell
mvn -Dspring-boot.run.profiles=mysql spring-boot:run
```

Or set the environment variable and run normally:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'mysql'; mvn spring-boot:run
```

Notes:
- The project already includes the MySQL JDBC driver in `pom.xml` (runtime scope).
- `spring.jpa.hibernate.ddl-auto=update` is enabled in the mysql profile to create/update schema for development only. For production, use migrations (Flyway/Liquibase) and a safer `ddl-auto` policy.

After the application starts with the MySQL profile, the seeded users (`librarian` / `student`) and initial books will be written to MySQL. You can then add books from the UI and issue them to students; the issuance table will also be persisted in MySQL.
## Building a JAR
```bash
cd C:\Users\student\Desktop\PBLJ
mvn clean package
# Run the JAR
java -jar target\library-management-0.0.1-SNAPSHOT.jar
```

## Project Structure (high-level)
- `src/main/java/com/example/library/`
  - `domain/` entities: `Book`, `UserAccount`, `Issuance`, `Role`
  - `repository/` Spring Data JPA repositories
  - `service/` business logic, fines/overdue
  - `security/` JWT, Spring Security config
  - `web/` REST controllers (auth, books, issuances, reports, backup, student)
  - `jobs/` scheduled tasks (overdue check)
  - `config/` seed data
- `src/main/resources/application.properties`
- `pom.xml`

## Troubleshooting
- `mvn` not recognized: install Maven or request Maven Wrapper to be added
- H2 console blocked: ensure security allows `/h2-console/**` (already configured) and headers frame options are disabled (configured)
- 401 errors: ensure you pass `Authorization: Bearer <JWT>` header from `/auth/login` response

## License
Educational/demo purposes.


