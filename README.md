# RunTable

A simple runtime-typed database implementation using a Kotlin DSL.

> **Note**: This implementation loads the entire `.rtdb` file into memory and is not suitable for large datasets.

---

## Features

### Table Creation
- Create a table with a single primary key and specify column types using Kotlin DSL.
- Example:

```kotlin
val table = table {
    path = "db/students.rtdb"
    name = "test"
    columns(primaryKey = "name") {
        "name".text
        "age".integer
        "is_student".boolean
    }
}
```
- File requirements:
    - The file must have a `.rtdb` extension.
    - If the file is empty, metadata (such as name, column definitions, and primary key) will be initialized automatically.

---

### Supported Data Types
- `STRING`, `INTEGER`, and `BOOLEAN` types are supported.
- The database ensures type integrity.

---

### CRUD Operations

#### **Select**
```kotlin
// Select the first record matching a condition
table.select.firstWhere(
    query { "name" eq "Josh" }
)

// Select all records matching a condition
table.select.allWhere(
    query { "is_student" eq true }
)

// Select and iterate over all records
table.select.all().forEach { println(it) }

// Select with advanced query conditions
val result = table.select.allWhere(
    query {
        ("age" lessThan 20) and ("age" moreThan 10) and ("name" contains "Al") or ("is_student" eq true)
    }
)
```

#### **Insert**
```kotlin
// Insert a single record
table.insert(
    record { "John" then 20 then true }
)

// Insert multiple records
table.insert.all(
    records {
        repeat(10) { i ->
            add { "Test$i" then i then Random.nextBoolean() }
        }
    }
)
```

#### **Delete**
```kotlin
// Delete the first record matching a condition
table.delete.firstWhere(
    query { "name" eq "John" }
)

// Delete all records matching a condition
table.delete.allWhere(
    query { "is_student" eq true }
)

// Delete all records
table.delete.all()
```

#### **Update**
```kotlin
// Update the first record matching a condition
table.update.firstWhere(
    query { "name" eq "John" },
    record { "John" then 20 then true }
)
```

---

### Query Operations
Queries support the following operations:
- `eq`: Equal to.
- `lessThan` / `moreThan`: Compare integer values.
- `contains`: Check if a string contains a specific substring.
- Logical operators: `and`, `or`.

**Example Query**:
```kotlin
query {
    ("age" lessThan 20) and ("age" moreThan 10) and ("name" contains "Al") or ("is_student" eq true)
}
```

---

## Database Structure
The `.rtdb` file follows this structure:

```
[name]
[column_1:STRING:P|column_2:INTEGER:N|column_3:BOOLEAN:N]
Data1|0|TRUE
Data2|1|FALSE
```

- **First line**: Database name.
- **Second line**: Column definitions, separated by `|`. Each column consists of three properties, separated by `:`:
    1. Column name.
    2. Column type (`STRING`, `INTEGER`, or `BOOLEAN`).
    3. Column role:
        - `P`: Primary key (only one primary key is allowed).
        - `N`: Non-primary key.
- **Subsequent lines**: Records, with properties separated by `|`.

> **Note**: Empty lines are ignored.

---

## Example

```kotlin
// Creating a table
val table = table {
    path = "db/students.rtdb"
    name = "test"
    columns("name") {
        "name".text
        "age".int
        "is_student".boolean
    }
}

// Deleting all records
table.delete.all()

// Inserting multiple records
table.insert.all(
    records {
        repeat(1000) { i ->
            add { "Test$i" then i then Random.nextBoolean() }
        }
    }
)

// Updating a record
table.update.firstWhere(
    query { "age" eq 20 },
    record { "John" then 20 then true }
)

// Selecting records with complex conditions
val result = table.select.allWhere(
    query {
        ("age" lessThan 20) and ("age" moreThan 10) and ("name" contains "Al") or ("is_student" eq true)
    }
).count()

println("Matching records count: $result")
```

---

## Project Example

This project includes example files to help you get started:

- **Database Example**:
  The `db` directory contains a sample `students.rtdb` file that demonstrates the database structure and usage.

- **Code Example**:
  The `src/main/kotlin/EXAMPLE.kt` file provides a complete Kotlin example showcasing table creation, data insertion, updates, and queries.

To explore these examples, navigate to the respective directories and review the provided files.
