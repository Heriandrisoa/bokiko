# Book Reader

<p align="center">
	<strong>A calm home for your docs.</strong><br>
	Upload your books, pick up where you left off, and read in the browser, anywhere, anytime.
</p>

<p align="center">
	<img alt="Java 17" src="https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white">
	<img alt="Jakarta EE 10" src="https://img.shields.io/badge/Jakarta%20EE-10-2c6b9a">
	<img alt="PDF.js" src="https://img.shields.io/badge/Reader-PDF.js-f04b37">
	<img alt="PostgreSQL" src="https://img.shields.io/badge/Database-PostgreSQL-4169e1?logo=postgresql&logoColor=white">
</p>

Book Reader is a self-hosted PDF library built with Jakarta EE. Each account has its own collection and saved reading progress, with a focused PDF.js reader for searching, zooming, bookmarking, and taking notes.

## What You Get

| Library | Reader |
| --- | --- |
| Account-based book collections | Searchable PDF text and page navigation |
| PDF upload and organization | Fit-to-width, fit-to-page, and custom zoom |
| Per-book reading progress | Bookmarks with notes |
| English, French, and Malagasy UI | Scroll-to-advance and touch page turns |

## Stack

- Java 17, Jakarta EE 10, Servlet 6.0, and JSP 3.1
- PostgreSQL for accounts, books, and reading progress
- PDF.js for in-browser rendering
- Maven and Jetty 12 for local development; deployable as a WAR

## Quick Start

### Requirements

- JDK 17 or newer
- Maven 3.9 or newer
- PostgreSQL 14 or newer

### 1. Create the database

Connect to PostgreSQL as a superuser and create the application role and database:

```sql
CREATE ROLE jee_user LOGIN PASSWORD 'change_me';
CREATE DATABASE jee_auth OWNER jee_user;
```

Create the tables from the project directory:

```powershell
psql -U jee_user -d jee_auth -f sql/schema.sql
```

### 2. Configure the application

Set values in `src/main/resources/db.properties`:

```properties
url=jdbc:postgresql://localhost:5432/jee_auth
user=jee_user
password=change_me
books.dir=C:/jee-auth-data/books
```

Environment variables override the corresponding properties:

| Property | Environment variable |
| --- | --- |
| `url` | `DB_URL` |
| `user` | `DB_USER` |
| `password` | `DB_PASSWORD` |
| `books.dir` | `BOOKS_DIR` |

### 3. Run locally

From the directory containing `pom.xml`:

```powershell
mvn jetty:run
```

Open [http://localhost:8080](http://localhost:8080). You can also start the app with `run.cmd` on Windows. Stop the server with `Ctrl+C`.

To use a different port:

```powershell
mvn jetty:run -Djetty.http.port=9090
```

Jetty serves `src/main/webapp` directly, so application changes do not require rebuilding the WAR.

## Build For Deployment

```bash
mvn clean package
```

The WAR is written to `target/jee-auth.war` and can be deployed to Tomcat 10.1 or newer. JSTL is bundled because Tomcat does not include it.

## Project Map

```text
src/main/java/          Servlets, filters, DAOs, and services
src/main/resources/     Database defaults and translations
src/main/webapp/        JSP views, styles, scripts, and PDF.js
sql/schema.sql          PostgreSQL schema
```

## Repository History

This repository was initialized from a recovered working copy after its prior Git metadata was unavailable. The first commit is an import snapshot; subsequent changes are recorded as ordinary, atomic commits.
