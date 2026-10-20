
# 📚 Library Management System

A simple and efficient **Library Management System** built using Java 21, JDBC, SQLite, and Maven. This console-based application helps manage library books, members, and book issue/return records with persistent database storage.

## ✨ Features

- 📖 **Book Management** — Add, view, search, update, and delete books.
- 👥 **Member Management** — Register new members and view all registered members.
- 📤 **Issue Books** — Issue available books to registered members.
- 📥 **Return Books** — Return issued books and update their availability.
- 🔍 **Search Issued Books** — Find currently issued books using a member's name.
- 🗃️ **SQLite Database** — Store books, members, and transaction history.
- 🔐 **Data Integrity** — Use unique ISBNs, unique email addresses, and foreign-key constraints.
- 🛠️ **Database Operations** — Use JDBC, prepared statements, and transactions.
- 📦 **Maven Support** — Manage project dependencies and build the application.

## 🧰 Technologies Used

| Technology | Purpose |
|---|---|
| Java 21 | Application logic and object-oriented programming |
| JDBC | Java database connectivity |
| SQLite | Local database storage |
| Maven | Dependency management and project build |
| SQL | Database queries and record management |

## 📁 Project Structure

```text
Library-Management-System/
├── src/
│   └── main/
│       └── java/
│           └── deepu/
│               └── library/
│                   ├── Main.java
│                   ├── Book.java
│                   ├── Member.java
│                   ├── Database.java
│                   └── LibraryRepository.java
├── docs/
├── pom.xml
├── .gitignore
└── README.md
```

## ⚙️ Prerequisites

Before running the application, ensure you have:

- Java Development Kit (JDK) 21
- Apache Maven
- Visual Studio Code or another Java-compatible IDE

Verify your Java and Maven installations:

```bash
java -version
mvn -version
```

## 🚀 Getting Started

### 1. Open the Project

Open the `Library-Management-System` folder in Visual Studio Code and open its terminal.

### 2. Compile the Project

Run the following command:

```bash
mvn clean compile
```

### 3. Run the Application

After a successful compilation, execute:

```bash
mvn exec:java
```

### 4. Use the Menu

Follow the instructions displayed in the terminal to manage books, register members, issue books, process returns, and view issued-book records.

## 🗄️ Database Design

The application uses **SQLite** for persistent local data storage. The database file `library.db` is created in the project root when the application initializes the database.

The database contains three tables:

- **books** — Stores book IDs, titles, authors, ISBNs, and availability status.
- **members** — Stores member IDs, names, and email addresses.
- **transactions** — Records book issue dates and return dates, along with references to books and members.

The application uses JDBC to execute database operations and prepared statements to safely handle SQL parameters.

> **Note:** The `library.db` file contains local application data and should not be committed to the Git repository.

## 🎯 Learning Outcomes

This project demonstrates:

- Object-oriented programming in Java
- JDBC connectivity with SQLite
- SQL queries and CRUD operations
- Prepared statements and exception handling
- Database transactions and foreign-key relationships
- Maven-based project configuration

## 👨‍💻 Author

**Deepu Bhardwaj**

---

*Built as a Java project to practice database connectivity, object-oriented programming, and application development.*
