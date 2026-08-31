# Nexa Bank Frequently Asked Questions

Policy ID: NEXA-FAQ-001  
Effective date: 2026-09-01  
Applies to: Nexa Bank demonstration environment

## Can the assistant see all bank accounts?

No. Live account and transaction tools operate under the verified JWT and may access only accounts authorized for that identity.

## Can the assistant transfer money immediately?

No. It must prepare the exact transfer details, show them to the user, receive explicit confirmation, and then execute using the unexpired confirmation token. Preparation alone never moves money.

## Where does balance information come from?

Balances come from authenticated banking tools and the account service. Policy documents are not a source of live balances.

## Where do fee and policy answers come from?

Fee, limit, eligibility, and timing answers come from retrieved policy documents. If relevant evidence is unavailable, the assistant must say that it could not find the information rather than inventing a policy.

## Are international transfers available in V1?

No. V1 cannot execute them through the digital transfer API. The fictional assisted-review process and demonstration charges are described in `international-transfer-policy.md`.

## How long is a prepared transfer valid?

Five minutes. An expired confirmation does not move money and a new preparation is required.

> These answers describe a fictional portfolio demonstration, not a real bank or public financial service.
