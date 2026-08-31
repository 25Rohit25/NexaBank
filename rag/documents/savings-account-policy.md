# Savings Account Policy

Policy ID: NEXA-POL-SAV-001  
Effective date: 2026-09-01  
Applies to: Nexa Bank demonstration environment

## Eligibility

A customer may open a savings account after registration and identity verification. The account must remain `ACTIVE` to accept deposits or participate in transfers. A `FROZEN` or `CLOSED` account cannot move money.

## Interest

The demonstration annual interest rate is 3.5%. Interest is calculated on the daily closing balance and credited monthly. The displayed rate may change for future periods, but an answer about the current rate must cite the effective policy version.

## Ownership and access

Account information is available only to the authenticated owner or an authorized administrator. A customer identifier supplied in a request body is not proof of ownership; the verified JWT identity is authoritative.

## Money movement

The account must have sufficient available balance for a transfer. Nexa Bank does not permit a transfer to create a negative balance. A successful operation must be supported by the deterministic banking service result, not inferred from an AI response.

> These figures are fictional portfolio-demo policy data, not real financial advice or a public offer.
