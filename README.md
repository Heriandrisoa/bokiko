# Book Reader

A Jakarta EE 10 (Servlet 6.0 / JSP 3.1) web application: register, sign in, upload PDFs, and read them
in the browser with PDF.js. Books and reading progress are stored per user in PostgreSQL.

## Repository history

This repository was initialized from a recovered working copy after its prior Git metadata was unavailable.
The first commit is therefore an import snapshot; subsequent changes are recorded as ordinary, atomic commits.

## Requirements

- JDK 17 or newer
- Maven 3.9 or newer
- PostgreSQL 14 or newer

## 1. Create the database

Connect to PostgreSQL as a superuser and run:

```sql
CREATE ROLE jee_user LOGIN PASSWORD 'change_me';
CREATE DATABASE jee_auth OWNER jee_user;
```

Then create the tables (safe to run more than once):

```bash
psql -U jee_user -d jee_auth -f sql/schema.sql
```

On Windows, `psql` is usually at `C:\Program Files\PostgreSQL\<version>\bin\psql.exe`
(or `E:\postgresql\bin\psql.exe` on this machine).

## 2. Configure the application

Defaults live in `src/main/resources/db.properties`:

```properties
url=jdbc:postgresql://localhost:5432/jee_auth
user=jee_user
password=change_me
books.dir=C:/jee-auth-data/books
```

Any value can be overridden with an environment variable, which takes precedence:

| Property    | Environment variable |
| ----------- | -------------------- |
| `url`       | `DB_URL`             |
| `user`      | `DB_USER`            |
| `password`  | `DB_PASSWORD`        |
| `books.dir` | `BOOKS_DIR`          |

## 3. Run it

Run Maven **from the project directory** (the directory containing `pom.xml`):

```powershell
cd D:\eclispse-workspace\book-reader
mvn jetty:run
```

Open <http://localhost:8080/>.

If you run Maven from another directory, you will get
`No plugin found for prefix 'jetty'`. In that case, point Maven at the POM or use the launcher script:

```powershell
# Option 1: explicit POM
mvn -f D:\eclispse-workspace\book-reader\pom.xml jetty:run

# Option 2: no prefix resolution, works from any directory
mvn -f D:\eclispse-workspace\book-reader\pom.xml org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:12.0.40:run

# Option 3: launcher script
D:\eclispse-workspace\book-reader\run.cmd
```

Stop with `Ctrl+C`. To use another port:

```powershell
mvn jetty:run -Djetty.http.port=9090
# or
.\run.cmd -Djetty.http.port=9090
```

The Jetty plugin compiles the sources and serves `src/main/webapp` directly, so Java/JSP/CSS/JS changes
are picked up without rebuilding the WAR.

## 4. Build a deployable WAR

```bash
mvn clean package
```

This produces `target/jee-auth.war`, which can be deployed to Tomcat 10.1 or newer (Tomcat 11 works too).
JSTL is bundled in the WAR because Tomcat does not ship it.

## Project layout

```
src/main/java/          Servlets, filters, DAOs, services
src/main/resources/     db.properties and i18n message bundles
src/main/webapp/        JSP views, CSS, JS, bundled PDF.js
src/main/webapp/WEB-INF/web.xml
sql/schema.sql          PostgreSQL schema
```
