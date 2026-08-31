# Transfer Limits

Policy ID: NEXA-POL-LIMIT-001  
Effective date: 2026-09-01  
Applies to: Nexa Bank demonstration environment

## V1 own-account transfers

Nexa Bank V1 supports INR transfers only between accounts owned by the same authenticated customer. Every transfer requires sufficient funds, an idempotency key, and the explicit confirmation flow exposed by the banking tools.

The demonstration policy limit is ₹100,000 per transfer and ₹250,000 in total per calendar day. These limits are policy information; a transfer is allowed only when the deterministic banking service confirms every applicable balance, ownership, status, and limit rule.

## Unsupported transfers

V1 does not execute transfers to external beneficiaries or international destinations. International requests follow `international-transfer-policy.md` and require assisted review.

## Confirmation expiry

A prepared AI transfer confirmation expires after five minutes. Expiry does not move money. After expiry, the transfer must be prepared again and the user must confirm the new details.

> These figures are fictional portfolio-demo policy data, not real financial advice or a public offer.
