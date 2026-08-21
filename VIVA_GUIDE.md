# MiniLang Translator — Academic Viva Voce Preparation Guide

This guide is structured to help you confidently defend your final-year project during external examination and project viva.

---

## Part 1: Fundamental Concepts

### Q1: What is the main objective of your project?
**Answer:**  
The objective is to design and implement a complete **Multi-Target Source-to-Source Translator (Transpiler)** in Java that accepts programs written in a custom language called **MiniLang** and compiles/translates them into three major target programming languages: **Python**, **Java**, and **C**.

### Q2: How is your translator different from a standard compiler (like `gcc` or `javac`)?
**Answer:**  
- A traditional compiler (like `gcc`) takes high-level source code and produces **low-level machine code** or assembly.
- A bytecode compiler (like `javac`) produces intermediate **JVM bytecode** (`.class` files).
- Our project is a **Source-to-Source Translator (Transpiler)**. It implements the full front-end and intermediate stages of a compiler (Lexer, Parser, AST, Semantic Analyzer, TAC IR, Optimizer) but generates **high-level source code** in multiple target languages rather than binary machine instructions.

### Q3: Why didn't you just use simple String replacement (Find & Replace)?
**Answer:**  
String replacement is purely lexical and lacks understanding of program structure:
1. It cannot determine **operator precedence** (e.g. `2 + 3 * 4` vs `(2 + 3) * 4`).
2. It cannot manage **variable scoping and shadowing** across blocks (`{ ... }`).
3. It cannot perform **type checking and semantic validation** (e.g., preventing assigning a string to an integer).
4. It can accidentally replace substrings within comments or string literals (e.g., replacing the word `print` inside a string `"please print this"`).

---

## Part 2: Front-End Phases (Lexer & Parser)

### Q4: What is the role of the Lexical Analyzer (Lexer)?
**Answer:**  
The Lexer scans the raw source code character by character, strips out whitespace and comments, and groups characters into meaningful syntactic units called **Tokens**. Each token contains:
- `TokenType` (e.g., `KW_INT`, `IDENTIFIER`, `PLUS`, `ASSIGN`)
- `lexeme` (the actual raw string, e.g., `"count"`, `"="`, `"42"`)
- `literalValue` (parsed value: integer, float, boolean, string)
- `line` and `column` numbers for accurate error reporting.

### Q5: What parsing technique did you implement?
**Answer:**  
We implemented a hand-crafted **Recursive-Descent Parser** with **Precedence Climbing**.  
- It processes tokens in a top-down manner.
- Each non-terminal grammar rule is represented by a dedicated Java method (e.g., `statement()`, `ifStatement()`, `logicalOr()`, `term()`, `factor()`).
- Operator precedence is strictly maintained by structuring method calls hierarchically from lowest precedence (`||`) down to highest precedence (`*`, `/`, unary `-`, `!`).

### Q6: What is an Abstract Syntax Tree (AST), and why is it necessary?
**Answer:**  
An **Abstract Syntax Tree (AST)** is an explicit hierarchical tree representation of the syntactic structure of source code, omitting superfluous syntactic tokens like semicolons and commas.  
It is essential because:
1. It decouples the language syntax from subsequent stages.
2. It allows multiple independent backends (Python, Java, C, TAC IR) to traverse and translate the exact same logical structure without reparsing.

---

## Part 3: Semantic Analysis & Symbol Table

### Q7: What errors are caught during Semantic Analysis that the Parser cannot catch?
**Answer:**  
The Parser only checks whether the code follows grammar rules. The Semantic Analyzer verifies the *meaning* and *validity* of the program:
1. **Undeclared Variables**: Using a variable before declaring it (e.g. `x = 10;` without `int x;`).
2. **Duplicate Declarations**: Declaring the same variable twice in the same scope (`int a; int a;`).
3. **Type Incompatibilities**: Attempting to assign `string` to `int` or perform arithmetic on boolean variables.
4. **Condition Types**: Ensuring `if` and `while` conditions evaluate strictly to `boolean`.

### Q8: How is the Symbol Table implemented in your project?
**Answer:**  
Our `SymbolTable` is a **scoped symbol table** implemented using a stack of hash maps (`Deque<Map<String, Symbol>>`).
- When a block `{` is entered, `enterScope()` pushes a new map.
- Variable lookups search from the current innermost scope outward to the global scope, enabling support for **variable shadowing**.
- When leaving a block `}`, `exitScope()` pops the current scope map.

---

## Part 4: Intermediate Code & Optimization

### Q9: What is Three-Address Code (TAC), and why is it used?
**Answer:**  
**Three-Address Code (TAC)** is a linearized intermediate representation where every instruction has at most one operator and at most three operand addresses (e.g., `t1 = b * 10`, `t2 = a + t1`, `c = t2`).  
It simplifies complex nested expressions into a universal, machine-independent format that is easy to optimize and analyze.

### Q10: What optimizations did you implement in your Code Optimizer?
**Answer:**  
We implemented four safe compile-time optimizations:
1. **Constant Folding**: Computing constant expressions at compile-time (e.g., `10 + 20` $\rightarrow$ `30`).
2. **Algebraic Simplification**: Applying mathematical identities (e.g., `x + 0` $\rightarrow$ `x`, `x * 0` $\rightarrow$ `0`, `x * 1` $\rightarrow$ `x`, `x && true` $\rightarrow$ `x`).
3. **Constant Propagation**: Propagating known constant assignments into subsequent expressions (e.g., `a = 10; b = a + 5;` $\rightarrow$ `b = 15;`).
4. **Dead Temporary Elimination**: Removing intermediate temporaries (`t1`, `t2`) that are never read.

---

## Part 5: Multi-Target Code Generation

### Q11: How does your architecture support adding a 4th target language (e.g., JavaScript or Rust)?
**Answer:**  
Our architecture adheres to the **Open-Closed Principle (OCP)** and the **Visitor Pattern**.  
To add a new target language:
1. Implement the `CodeGenerator` interface and `ASTVisitor<String>`.
2. Map AST nodes to the target language syntax.
3. Register the new generator in `MiniLangTranslator.java` and JavaFX UI.  
No changes to the Lexer, Parser, AST, Semantic Analyzer, or Optimizer are required.
