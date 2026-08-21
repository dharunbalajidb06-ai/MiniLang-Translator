# Final-Year Project Presentation (20-Slide Structure)

## Project Title:
**Design and Implementation of a Multi-Target Programming Language Translator Using Java**

---

### Slide 1: Title Slide
- **Project Title**: Design and Implementation of a Multi-Target Programming Language Translator Using Java
- **Source Language**: MiniLang
- **Target Languages**: Python, Java, C
- **Student Name**: [Your Name] | **Register No**: [Your Register Number]
- **Department**: Computer Science and Engineering
- **Supervisor / Guide**: [Guide Name]

---

### Slide 2: Introduction & Motivation
- Need for cross-language migration and interoperability in modern computing.
- Why building a true compiler frontend is superior to heuristic find-and-replace tools.
- Educational and engineering significance of compiler construction from scratch.

---

### Slide 3: Problem Statement
- Heterogeneous target environments require translating source logic across different syntax paradigms.
- Challenge: Designing a unified language frontend that verifies static semantics and emits idiomatic, safe code across dynamically typed (Python) and statically typed (Java, C) environments.

---

### Slide 4: Existing Systems vs Limitations
- **GCC / Clang**: Target machine-dependent assembly; black-box internal states.
- **Transpilers (Babel/TypeScript)**: Highly complex, single-target, heavy toolchains.
- **Naive Regex Converters**: Lack operator precedence, scoping, and type verification.

---

### Slide 5: Proposed System Overview
- **MiniLang**: A clean imperative programming language.
- Full 6-stage compiler pipeline implemented in **Java 17+**.
- Common Intermediate Representation (Three-Address Code).
- Pluggable multi-target backends for Python, Java, and C.
- JavaFX Visual Stage Inspector.

---

### Slide 6: Project Objectives
1. Define MiniLang syntax and formal BNF grammar.
2. Implement Lexer, Recursive-Descent Parser, and AST.
3. Build Scoped Symbol Table and Static Semantic Analyzer.
4. Generate and Optimize Three-Address Code (TAC).
5. Implement Python, Java, and C Code Generators.
6. Provide JavaFX GUI and automated JUnit 5 test suite.

---

### Slide 7: Overall System Architecture
- Architectural Diagram showing:
  `MiniLang Source` $\rightarrow$ `Lexer` $\rightarrow$ `Parser` $\rightarrow$ `AST` $\rightarrow$ `Semantic Analyzer` $\rightarrow$ `Symbol Table` $\rightarrow$ `TAC IR` $\rightarrow$ `Optimizer` $\rightarrow$ `Code Generators (Python / Java / C)`

---

### Slide 8: MiniLang Language Design
- **Data Types**: `int`, `float`, `string`, `boolean`
- **Operators**: Arithmetic (`+`, `-`, `*`, `/`, `%`), Relational (`>`, `<`, `==`...), Logical (`&&`, `||`, `!`)
- **Control Flow**: `if-else`, `while`
- **I/O**: `print()`, `input()`

---

### Slide 9: Lexical & Syntax Analysis
- **Lexer**: Character-by-character scanner with line & column tracking.
- **Recursive-Descent Parser**: Top-down parsing with operator precedence climbing.
- Syntax error handling with precise source locations.

---

### Slide 10: Abstract Syntax Tree (AST) & Visitor Pattern
- Object-oriented hierarchy: `StatementNode`, `ExpressionNode`, `BinaryExprNode`, etc.
- Separation of concerns using the **Visitor Design Pattern** (`ASTVisitor<R>`).
- Hierarchical ASCII Tree pretty printer for visualization.

---

### Slide 11: Semantic Analysis & Scoped Symbol Table
- Detection of **undeclared variables** and **duplicate definitions**.
- Type checking: Assignment compatibility, boolean conditions in control flow.
- Scoped Symbol Table using `ArrayDeque` to support block scoping and variable shadowing.

---

### Slide 12: Intermediate Representation (Three-Address Code)
- Linearized instruction format: `result = arg1 op arg2`.
- Automatic temporary variable generation (`t1`, `t2`...) and branch label allocation (`L1`, `L2`...).

---

### Slide 13: Code Optimization Engine
- **Constant Folding**: e.g., `10 + 20` $\rightarrow$ `30`
- **Algebraic Simplification**: e.g., `x + 0` $\rightarrow$ `x`, `x * 0` $\rightarrow$ `0`
- **Constant Propagation**: Propagating known constant assignments.
- **Dead Temporary Elimination**: Removing unused intermediate variables.

---

### Slide 14: Multi-Target Code Generation
- **Python**: Indentation management, boolean conversion (`True`/`False`), dynamic typing.
- **Java**: Enclosing class structure, `Scanner` input, static typing.
- **C**: `#include <stdio.h>`, formatted `printf`/`scanf`, memory buffers.

---

### Slide 15: JavaFX Graphical User Interface
- Dark-mode code editor with live syntax translation.
- 8-Tab inspection panel:
  `Target Code` | `Tokens` | `AST` | `Symbol Table` | `TAC` | `Optimized TAC` | `Performance` | `Diagnostics`

---

### Slide 16: Automated Testing & Validation
- **34 automated JUnit 5 test cases** passing with 100% success rate.
- Positive validation tests across all language constructs.
- Negative error tests (lexical errors, syntax errors, type mismatch errors).

---

### Slide 17: Performance Evaluation
- Microsecond-level stage timing measurement.
- Total translation time under **0.5 milliseconds** per program.
- High throughput and negligible memory footprint.

---

### Slide 18: Key Advantages & Impact
- Zero external compiler libraries (pure Java 17 implementation).
- Clean, decoupled, modular architecture adhering to SOLID principles.
- Ideal pedagogical tool for computer science compiler courses.

---

### Slide 19: Live Project Demonstration
- [Execute JavaFX GUI with sample programs]
- Demonstration of:
  1. Arithmetic computation $\rightarrow$ Python / Java / C translation.
  2. While loop factorial computation.
  3. Intentional type mismatch error detection.

---

### Slide 20: Conclusion & Future Work
- Summary of project achievements.
- Future Enhancements: User-defined functions, arrays, direct JVM bytecode output.
- **Questions & Answers (Thank You!)**
