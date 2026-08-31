# Transfer Refund and Error Policy

Policy ID: NEXA-POL-REFUND-001  
Effective date: 2026-09-01  
Applies to: Nexa Bank demonstration environment

## Failed or incomplete transfers

If the banking service reports a transfer as failed, no success should be communicated to the customer. For an atomic own-account transfer, both debit and credit succeed together or both fail. A pending or unknown result must be investigated using the transfer and transaction identifiers.

## Duplicate requests

Retrying the same transfer with the same idempotency key returns the original result and must not create a second transfer. Reusing that key with different transfer details is rejected.

## Customer-reported errors

A customer should report an incorrect or unrecognized transfer promptly and provide the transfer identifier. Nexa Bank records the report, investigates the audit and ledger entries, and communicates the outcome. Filing a report does not guarantee reversal.

## Timing

The demonstration target is to acknowledge a report within one business day and complete straightforward own-account investigations within five business days. Complex or externally reviewed cases may take longer.

> These figures are fictional portfolio-demo policy data, not real financial advice or a public offer.
