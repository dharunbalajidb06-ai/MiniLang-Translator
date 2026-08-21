# MiniLang Multi-Target Programming Language Translator

A complete, production-grade **Source-to-Source Translator (Transpiler)** built in **Java 17+** with **Maven** and **JavaFX**. It compiles custom **MiniLang** programs through the full compiler pipeline and generates equivalent, idiomatic code in **Python**, **Java**, and **C**.

---

## 🏛️ Compiler Pipeline Architecture

```
MiniLang Source Code (.ml)
        ↓
1. Lexical Analyzer (Lexer)
        ↓  Tokens stream
2. Syntax Analyzer (Recursive-Descent Parser)
        ↓  Abstract Syntax Tree (AST)
3. Semantic Analyzer & Scoped Symbol Table
        ↓  Type-checked & Validated AST
4. Intermediate Representation (IR / TAC Generator)
        ↓  Three-Address Code (TAC)
5. Code Optimizer (Constant Folding & Propagation)
        ↓  Optimized Three-Address Code
6. Multi-Target Code Generators
 ┌──────────────┼──────────────┐
 ↓              ↓              ↓
Python         Java            C
(.py)         (.java)        (.c)
```

---

## 🚀 Key Features

- **MiniLang Language Support**:
  - **Data Types**: `int`, `float`, `string`, `boolean`
  - **Variable Declarations & Assignments**: Explicitly typed with scope validation
  - **Arithmetic Operators**: `+`, `-`, `*`, `/`, `%`
  - **Relational Operators**: `>`, `<`, `>=`, `<=`, `==`, `!=`
  - **Logical Operators**: `&&`, `||`, `!`
  - **Control Flow**: `if (...) { ... } else { ... }`, `while (...) { ... }`
  - **I/O Functions**: `print(expr);`, `input(var);`
  - **Comments**: Single-line `// ...` and Multi-line `/* ... */`
- **Compiler Phases**:
  - **Lexer**: Character-by-character scanner tracking precise line & column numbers.
  - **Parser**: Hand-crafted Recursive-Descent parser enforcing strict operator precedence.
  - **AST**: Full OOP AST hierarchy with `ASTVisitor` and ASCII tree pretty printer.
  - **Symbol Table**: Scoped symbol table with support for nested block scopes and shadowing.
  - **Semantic Analyzer**: Strong type checking, declaration enforcement, condition verification.
  - **IR (TAC)**: Quadruple-based Three-Address Code with temporary variable (`t1`, `t2`...) and label (`L1`, `L2`...) generation.
  - **Optimizer**: Multi-pass constant folding, algebraic simplification, constant propagation, and dead temporary removal.
  - **Multi-Target Generators**:
    - **Python 3**: Idiomatic indentation, dynamic typing, boolean formatting.
    - **Java 17**: Enclosed `GeneratedProgram` class with `Scanner` and static typing.
    - **C99/C11**: `#include <stdio.h>`, formatted `printf`/`scanf`, arrays for strings.
- **JavaFX GUI**:
  - Dual-pane IDE with dark code editor.
  - Multi-tab inspection: 🏷️ Tokens, 🌳 AST, 📋 Symbol Table, ⚙️ TAC, ⚡ Optimized TAC, 💻 Target Code, ⏱️ Performance Profiler, ⚠️ Diagnostics.
- **CLI Support**:
  - Batch compile MiniLang files to `.py`, `.java`, or `.c`.

---

## 📂 Project Structure

```
MiniLangTranslator/
│
├── pom.xml
├── README.md
├── examples/
│   ├── 01_arithmetic.ml
│   ├── 02_if_else.ml
│   ├── 03_while_loop.ml
│   └── 04_fibonacci.ml
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/minilang/
│   │           ├── MiniLangTranslator.java     # Main pipeline coordinator
│   │           ├── TranslationResult.java      # Pipeline output container
│   │           ├── lexer/                      # Lexer, Token, TokenType
│   │           ├── parser/                     # Recursive-Descent Parser
│   │           ├── ast/                        # AST hierarchy & ASTPrinter
│   │           ├── symbol/                     # Symbol, SymbolTable, Type
│   │           ├── semantic/                   # SemanticAnalyzer
│   │           ├── ir/                         # TACOp, TACInstruction, TACGenerator
│   │           ├── optimizer/                  # Constant Folding & Propagation
│   │           ├── codegen/                    # CodeGenerator interface
│   │           │   ├── python/                 # PythonCodeGenerator
│   │           │   ├── java/                   # JavaCodeGenerator
│   │           │   └── c/                      # CCodeGenerator
│   │           ├── errors/                     # Compiler error diagnostics
│   │           ├── cli/                        # MainCLI
│   │           └── gui/                        # MainApp (JavaFX UI)
│   │
│   └── test/
│       └── java/
│           └── com/minilang/                   # 34+ automated JUnit 5 tests
```

---

## 🛠️ How to Build and Run

### Prerequisites
- **Java 17 or later** (`java -version`)
- **Maven 3.8+** (`mvn -version`)

### 1. Run Automated Unit Tests
```bash
mvn clean test
```

### 2. Launch the JavaFX Graphical User Interface
```bash
mvn javafx:run
```

### 3. Run Command-Line Translator (CLI)
```bash
mvn compile exec:java -Dexec.mainClass="com.minilang.cli.MainCLI" -Dexec.args="examples/01_arithmetic.ml -t python"
```
Or for C / Java:
```bash
mvn compile exec:java -Dexec.mainClass="com.minilang.cli.MainCLI" -Dexec.args="examples/02_if_else.ml -t c -o output.c"
```
