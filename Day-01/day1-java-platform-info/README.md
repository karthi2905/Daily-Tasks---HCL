# Day 1 — Java Platform Information

### Objective

Demonstrate basic Java command-line development using the JDK tools:

- `javac` (compiler)
- `java` (application launcher)
- `javap` (bytecode disassembler)
- `java -verbose:class` (class loading diagnostics)

---

### Requirements

- JDK 21 (or compatible LTS JDK)
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
Generates `PlatformInfo.class` bytecode file in the current directory.

#### 2. Run the Program
```bash
java PlatformInfo
```
Example Output:
```text
===== JAVA PLATFORM INFORMATION =====
Java Version      : 25.0.1
Operating System  : Windows 11
Processors        : 12
Max Heap (bytes)  : 4217372672
Free Heap (bytes) : 264024344
```

#### 3. Inspect Bytecode with `javap`
```bash
javap -c PlatformInfo
```
`javap -c` disassembles the compiled `.class` file and prints the JVM bytecode instructions (such as `getstatic`, `ldc`, `invokevirtual`, `invokestatic`, `return`).

#### 4. Run with Verbose Class Loading
```bash
java -verbose:class PlatformInfo
```
Displays all classes dynamically loaded by the JVM during execution (including `java.lang.Object`, `java.lang.String`, `java.lang.System`, `java.lang.Runtime`).

---

### Environment & JDK Verification

```bash
java -version
javac -version
where java
where javac
```
