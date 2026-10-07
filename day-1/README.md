# Day 1 — Java Platform Information

### Objective

Demonstrate basic Java command-line development using standard JDK tools without an IDE:

- `javac` (compiler)
- `java` (runtime launcher)
- `javap` (bytecode disassembler)
- `java -verbose:class` (class loading diagnostics)

---

### Requirements

- JDK 21 (or modern LTS JDK)
- Terminal / Command Prompt
- Pure command-line toolchain (**No IDE**)

---

### Program Description

[`PlatformInfo.java`](./PlatformInfo.java) inspects the host Java runtime and system environment using core Java platform APIs:

- **Java Version**: `System.getProperty("java.version")`
- **Operating System**: `System.getProperty("os.name")`
- **Available Processors**: `Runtime.getRuntime().availableProcessors()`
- **Maximum Heap Memory**: `Runtime.getRuntime().maxMemory()`
- **Free Heap Memory**: `Runtime.getRuntime().freeMemory()`

---

### Execution Flow

```text
PlatformInfo.java
       │
       │ javac PlatformInfo.java
       ▼
PlatformInfo.class
       │
       │ java PlatformInfo
       ▼
Console Output
```

---

### Step-by-Step Command-Line Verification

#### 1. Compile the Program
```bash
javac PlatformInfo.java
```
Compiles Java source directly to bytecode, creating `PlatformInfo.class`. Note that `*.class` files are build artifacts and must not be committed to Git.

#### 2. Run the Program
```bash
java PlatformInfo
```
Example Output:
```text
===== JAVA PLATFORM INFORMATION =====
Java Version      : 25.0.1
Operating System  : Windows 11
Processors        : 16
Max Heap (bytes)  : 4135583744
Free Heap (bytes) : 258845024
```

#### 3. Inspect Bytecode with `javap`
```bash
javap -c PlatformInfo
```
`javap -c` disassembles the compiled `.class` file and prints the JVM bytecode instructions (such as `getstatic`, `ldc`, `invokevirtual`, `invokestatic`, `invokedynamic`, `return`).

#### 4. Run with Verbose Class Loading
```bash
java -verbose:class PlatformInfo
```
Displays class-loading diagnostic logs indicating all classes loaded by the JVM during execution (including `java.lang.Object`, `java.lang.String`, `java.lang.System`, `java.lang.Runtime`).

---

### Environment & JDK Verification Commands

```bash
java -version
javac -version
where java
where javac
```
