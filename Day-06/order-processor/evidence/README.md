# Evidence Directory :: Copilot Stack Trace Review

This directory contains the documentation and review templates for **Day 6 — Order Processor: Exception Handling & Copilot Stack Trace Review**.

## Contents

- **[`copilot-review.md`](./copilot-review.md)**: Contains the genuine stack traces from Scenario 1 and Scenario 2, paste-ready prompts for GitHub Copilot Chat, claim-by-claim verification rubrics, and the authoritative root cause analyses.

## How to Reproduce Stack Traces

Ensure you are in the `Day-06/order-processor/` directory:

### Scenario 1 — Invalid Quantity (Unchecked `InvalidQuantityException`)
```bash
# Direct Java execution
java -cp out com.technova.orders.app.OrderApplication --scenario1

# Or using batch runner
run.bat --scenario1
```

### Scenario 2 — Chained Insufficient Stock (Checked `OrderProcessingException` -> `InsufficientStockException`)
```bash
# Direct Java execution
java -cp out com.technova.orders.app.OrderApplication --scenario2

# Or using batch runner
run.bat --scenario2
```

## Manual Copilot Chat Review Steps

1. **Open IDE & Copilot Chat**: Open VS Code or IntelliJ IDEA with the GitHub Copilot Chat panel active.
2. **Copy Prompt for Trace 1**: In [`copilot-review.md`](./copilot-review.md), copy the code snippet under `### Copilot Prompt to Copy & Paste` for Trace 1.
3. **Submit to Copilot**: Send the prompt to Copilot Chat and wait for its analysis.
4. **Record Response**: Copy Copilot's response verbatim into `### Copilot's explanation`.
5. **Verify Claims**: Evaluate each claim made by Copilot using the reference code in `src/com/technova/orders/` and mark each as **Correct**, **Partially correct**, or **Incorrect** with supporting evidence.
6. **Repeat for Trace 2**: Perform the same sequence for Trace 2 (Chained Exception).
7. **Commit & Push**: Save and commit the updated `evidence/copilot-review.md`.
