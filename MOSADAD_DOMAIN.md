# Mosadad — Business Domain Reference

This is the canonical source of truth for what the Mosadad app does, used to
drive test design across the framework in this repo. If a test's intent is
unclear, check here first; if the app's real behavior ever contradicts this
document, the document is out of date — update it as part of that change.

**App under test:** UAE-based Insurance Recovery platform.
**QA URL:** `https://recoveryclaim-entity-qa.azurewebsites.net/auth/login`

---

## Actors

| Role | Purpose |
|---|---|
| **Claimant Insurer** | The insurer asking to be reimbursed. Creates and drives recovery claims. |
| **At-Fault Insurer** | The insurer responsible for paying. Responds to claims, quotations, invoices. |
| **Regulator** | Monitors disputes, response times, and settlement patterns across the market. |
| **Mosadad Admin** | Manages system access and governance. |

There are **38 insurers** registered in the UAE using the platform.

Only a **Claimant Insurer** test account exists in this framework today
(`Dubaiqa@gmail.com`, verified live — lands on `/entity-landing/dashboard`
with the "Entity Modules" sidebar: Home, Recovery Claims, Company Details).
At-Fault Insurer, Regulator, and Mosadad Admin accounts are not yet
provisioned; any test requiring a cross-role interaction is a stub until
those credentials are available.

---

## Claim Lifecycle

A recovery claim moves through four sequential stages. Every stage after
Stage 1 depends on the At-Fault insurer responding within an SLA window.

### Stage 1 — Claim Registration

The Claimant Insurer creates a recovery claim, entering:

- Claim number
- Policy details
- Vehicle details
- Accident details
- Official Police Report reference number
- Supporting documents

**Hard rule:** recovery is based on the official Police Report. Without a
valid accident report reference, recovery cannot proceed.

Three ways to create a claim:

1. **Manual Entry** — insurer types information directly.
2. **Police Data Entry** — accident details entered based on the official
   Police Report issued by Dubai Police, Rafid, Saeed, or another UAE
   Police authority.
3. **Fast Track (Bulk Excel Upload)** — for uploading many claims at once.

On submission, the **At-Fault insurer is automatically notified**.

### Stage 2 — Quotation

Repair cost is submitted here. Two mutually exclusive paths:

#### A. Normal Repair

1. Claimant uploads: workshop estimate, repair amount, supporting documents.
2. System sends a notification and starts a **72-hour timer**.
3. At-Fault insurer must respond within **3 working days**: Approve or
   Negotiate.
4. Negotiation happens inside the system until both sides agree.
5. Once agreed, the system generates an **Approval Letter**.

#### B. Total Loss

Triggered when vehicle damage is severe and repair is not economical.

1. Claimant submits: sum insured, total loss amount, supporting documents.
2. System sends a notification and starts a **7-day timer (168 hours)**.
3. If approved, the **Salvage** stage begins — selling the damaged vehicle
   for scrap value:
   - Claimant enters salvage buyer details and salvage amount.
   - System automatically adds **5% VAT** and updates the total claim
     amount.
4. Once salvage is approved, the claimant uploads the invoice and Stage 3
   begins.

### Stage 3 — Invoice

1. Claimant uploads: final repair invoice number, credit note paid to
   workshop, supporting documents.
2. System generates a recovery letter and notifies the At-Fault insurer.
3. At-Fault insurer Approves or Negotiates the invoice.
4. Once approved, the Settlement stage begins.

### Stage 4 — Settlement

1. At-Fault insurer enters the credit note number and uploads the credit
   note copy.
2. Once done, claim status becomes **Closed**. Recovery is complete.

---

## Fast Track — Why It Exists

Insurers may hold 50, 100, or 200+ small recovery claims against the same
counterpart insurer; creating each one manually is slow. Fast Track offers:

- Bulk upload using Excel
- Automatic validation of required columns
- Duplicate claim detection
- Batch processing

The At-Fault insurer can download the same file, enter credit note numbers,
and upload the updated sheet. Fast Track targets **high-volume, simple
recoveries**, trading per-claim detail for throughput.

---

## Disputes

If insurers disagree on **liability**, **repair amount**, or **invoice**, a
structured dispute is raised inside Mosadad instead of over email:

1. Claimant raises a dispute.
2. At-Fault insurer responds.
3. All communication is logged.
4. Timestamps are recorded.
5. **Undisputed amounts can still move forward** — a dispute on one line
   item doesn't block the rest of the claim.

If unresolved, the **Regulator** can see the full dispute history. This is
the transparency/accountability mechanism replacing informal email
back-and-forth.

---

## SLA Timers

| Quotation type | SLA window |
|---|---|
| Normal Repair | 72 hours |
| Total Loss | 168 hours (7 days) |

Purpose: faster resolution, no silent delays, a fair and trackable process.
**Missed deadlines are recorded** — this is what feeds the Regulator's
"SLA Violation" view (a real screen, confirmed live at
`/entity-portal/sla-violation`).

---

## Wallet, Settlement Rail & UAE PASS

### Mosadad Wallet

Each insurer maintains, per the platform's digital wallet:

- Payable position
- Receivable position
- Settled amounts
- Outstanding amounts

The wallet does **not** replace a bank transfer — it records and structures
settlement confirmation digitally, giving clear reconciliation, no
paid/unpaid confusion, transparent financial exposure, and Regulator
visibility into settlement behavior. (A "My Wallets" entry point was
confirmed live on the entity dashboard.)

### Digital Settlement Flow

1. Claim approved
2. Invoice approved
3. At-Fault insurer issues credit note
4. Settlement recorded in Mosadad
5. Wallet updated
6. Claim marked **Closed**

This is the structured financial settlement rail between insurers that
Stage 4 (Settlement) implements.

### UAE PASS

UAE PASS is the UAE's official digital identity system, providing secure
login, identity verification, and legal digital authentication. Mosadad
uses it because recovery claims carry financial and legal responsibility,
so access must be secure and verified. **Not yet exercised by this
framework** — the QA login used here is plain email/password, not a UAE
PASS flow; if the app supports both, note which path a given test exercises.

---

## What's Verified vs. Stubbed in This Framework

See `FRAMEWORK.md` for the technical decisions. In domain terms:

- **Verified live (2026-08-30), Claimant Insurer role:** login, dashboard
  landing, and all 8 links across the three dashboard module cards
  (Dashboard: Claims Report, SLA Violation · Recovery Claim Records:
  Potential Recovery Claims, Recovery Claims List, Fast Track · Financial:
  Bulk Settlement, Due Amount, Payment History).
- **Not yet explored (UI):** the actual "Create Claim" flow and all three
  entry methods, the Quotation/Total Loss/Salvage screens, Invoice,
  Settlement, Dispute, Wallet detail, and any At-Fault Insurer / Regulator /
  Mosadad Admin screens (no credentials for those roles yet).
- **Verified live (2026-09-17), API layer:** the real backend — six
  microservices (claims, inthub, invoice, quotation, settlement, tenant)
  behind a shared Azure API Management gateway — and 403 API test methods
  covering all 359 documented endpoints (330 live-verified, 73 disabled
  stubs for real-data mutations pending sign-off). See FRAMEWORK.md's API
  section for the base URL, auth flow, response-envelope shapes, the known
  unscoped bulk-action endpoints to never call ad hoc, and the backend bugs
  found along the way.
