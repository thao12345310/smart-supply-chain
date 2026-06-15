---
name: datn-writing
description: Use when writing, editing, reviewing, or polishing the graduation thesis (DATN / đồ án tốt nghiệp) in SOICT_DATN_Application_ENG_Template — any task touching NoiDung/*.tex, thesis chapters, abstract, contributions, citations, or academic English for the Smart Supply Chain project.
---

# DATN Writing — Smart Supply Chain Management System

Academic-English graduation thesis for HUST SoICT, Global Engineer Program.
Supervisor: Pham Quang Dung. Title: *Building a software for distribution management*.
The full author-facing guide lives at `SOICT_DATN_Application_ENG_Template/HUONG_DAN_VIET_DATN.md` — read it for diagram lists, page budgets, and prep checklists.

## Hard rules — never break

1. **Edit ONLY `SOICT_DATN_Application_ENG_Template/NoiDung/*.tex`.** Never touch `main.tex`, `main_datn.tex`, `Chapter/*`, `Cover*.tex`, the preamble, fonts, margins, or any format/recipe. The template is frozen; only content changes.
2. **The thesis is in English.** Academic, objective, third-person. No emotive words ("great", "amazing", "tuyệt vời").
3. **Build to verify after edits:** `cd SOICT_DATN_Application_ENG_Template && latexmk -pdf -outdir=build main_datn.tex` → `build/main_datn.pdf`. A change that breaks the build is not done.

## Quality rules (from template Appendix A — graded)

- Write **complete paragraphs**, never slide-style bullet lists. One idea + analysis per paragraph.
- **Every figure/table/equation must be referenced with `\ref{}` and explained** in prose at least once. Figure caption *below*, table caption *above*.
- **Cite everything not your own** with `\cite{key}` (entries in `reference.bib`, IEEE style). No Wikipedia, no lecture slides. Target 15–25 references. Plagiarism = no defense.
- Enumerate scientifically and consistently: (i), (ii), (iii).
- Each chapter has an opening sentence (linking the previous chapter) and a closing sentence (summary + bridge to the next).
- Diagrams: insert with `\thesisfig{name}{Caption}{fig:label}` (21 PlantUML diagrams already rendered in `Figure/diagrams/`). Code: `lstlisting` (Java/SQL/JSON configured in `lstlisting.tex`); algorithms: `algorithm2e`.

## Repo grounding — the core discipline

This is the project's edge over a generic thesis: **every claim about the system must map to real code or a real number in this repo.** No vague generalities. When writing a system claim, open the source and cite the actual class/method/commit.

Key artifacts (backend at `backend/src/main/java/com/distribution/`):

| Claim | Where it lives |
|-------|----------------|
| FEFO lot allocation | `service/impl/GoodsIssueServiceImpl.java`, `repository/InventoryLotRepository.java` (`findAvailableLotsFEFO`) |
| Inventory reservation on SO approval | `service/impl/SalesOrderServiceImpl.java` |
| Failed-delivery recovery | `service/impl/DeliveryTripServiceImpl.java` (`restockFromFailedDelivery`) |
| RBAC (endpoint/method/data) | Spring Security config + method annotations + service-level checks |
| **N+1 fix (2380ms → 47ms)** | commit `20d711f0` "perf(delivery): fix N+1 in listAvailable" |
| Code statistics | `cloc backend/src frontend/src` (don't guess; measure) |

## Chapter map & page budget

| File | Chapter | Pages | Focus |
|------|---------|-------|-------|
| `01_introduction.tex` | Introduction | 3–6 | motivation → objectives/scope → tentative solution |
| `02_survey.tex` | Requirement Survey & Analysis | 9–11 | status survey, use cases, functional + non-functional |
| `03_background.tex` | Background & Technologies | ≤10 | each tech: which requirement → alternatives → why chosen (**cite**) |
| `04_design_impl.tex` | Design, Implementation, Evaluation | 15–25 | architecture, class/ER/sequence, screenshots, tests, deployment |
| `05_contribution.tex` | Solution & Contribution | ≥5 | **highest-graded.** Each contribution = (i) problem (ii) solution (iii) result |
| `06_conclusion.tex` | Conclusion & Future Work | 2–4 | results vs competitors, future directions |

Recommended drafting order: Ch.3 → Ch.2 → Ch.4 → Ch.5 → Ch.1 → Ch.6 → Abstract → Acknowledgment.

## Workflow per writing request

1. Read the current `NoiDung/<file>.tex` section + the relevant source code first.
2. Write/edit only that section in prose, grounded in real code/numbers, with `\cite`/`\ref` where required.
3. Build; fix any LaTeX error.
4. Flag to the author anything only they can supply (real screenshots, Figma mockups, content sign-off for plagiarism).

## Red flags — stop and fix

- About to edit a file outside `NoiDung/` → stop.
- A system claim with no source file behind it → open the code or cut the claim.
- A figure/table with no `\ref` in prose → add the reference + explanation.
- A borrowed idea/fact with no `\cite` → add the reference.
- Slide-style bullets in body text → rewrite as paragraphs.
