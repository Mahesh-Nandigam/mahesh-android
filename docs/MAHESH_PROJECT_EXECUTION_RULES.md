# 🛡️ "MAHESH-" — PROJECT EXECUTION RULES & 5-POINT VERIFICATION PROTOCOL

---

## 📌 RULE 1: STRICT 5-POINT SELF-VERIFICATION PROTOCOL
Before writing or executing any task from the 170-task master plan, the agent must perform and document the following 5-point verification:

1. **[Scope Verification]** Check task ID, description, and module boundaries against `HEY_MAHESH_160_TASK_MASTER_PLAN.md`.
2. **[Architecture & Code Quality]** Ensure Clean Architecture, idiomatic Kotlin 2.0+ / Compose patterns, and no tight coupling.
3. **[Zero-Crash & Memory Safeguards]** Verify proper coroutine scopes, thread safety, null-safety, and zero main-thread blocking.
4. **[Test & Integrity Parity]** Ensure every logic component has automated tests and does not break previous tasks.
5. **[User Permission Protocol]** Always present the exact task to the user, display the 5-point verification results, and ask for explicit approval before proceeding.

---

## 📌 RULE 2: TESTING RIGOR
* **Modules 01–03:** Tested purely on PC (JVM unit tests, lint checks, compiler verification) for rapid, zero-friction velocity.
* **Module 05:** First physical device test milestone (Voice & Wake-Word).
* **Module 07:** Second physical device test milestone (Phone Calls & CameraX).
* **Module 09:** Third physical device test milestone (Accessibility Screen Pilot & Glowing Overlay).

---

## 📌 RULE 3: CODE PRESERVATION & DOCUMENTATION
* Every single file created must be clean, self-contained, documented, and production-grade.
* No temporary hacks, no hardcoded API keys in client code, and no unhandled exceptions.
