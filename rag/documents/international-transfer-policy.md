# International Transfer Policy

Policy ID: NEXA-POL-INTL-001  
Effective date: 2026-09-01  
Applies to: Nexa Bank demonstration environment

## Availability

International transfers are not executable through the Nexa Bank V1 digital transfer API. The V1 API supports only INR transfers between accounts owned by the same authenticated customer. An international transfer request must be referred for assisted review and must never be represented as completed unless an authorized international-transfer service returns success.

## Eligibility and review

Assisted international transfers require an active account, completed identity verification, a verified beneficiary, a declared purpose, and any documents requested during compliance review. Nexa Bank may reject or delay a request for sanctions, anti-money-laundering, foreign-exchange, or beneficiary-validation checks.

## Fees and exchange rate

The demonstration service fee is 1% of the INR transfer amount, subject to a minimum of ₹250 and a maximum of ₹1,500. A 2% foreign-exchange markup applies to the reference exchange rate. Applicable taxes and charges imposed by intermediary or receiving banks are additional and may not be known in advance.

For a ₹50,000 transfer, the Nexa service fee is ₹500 and the foreign-exchange markup is ₹1,000, so the known Nexa charges are ₹1,500 before taxes and third-party bank charges.

## Timing

An approved transfer normally reaches the receiving bank within one to three business days. Compliance review, incorrect beneficiary details, bank holidays, and intermediary banks may extend this time.

> These figures are fictional portfolio-demo policy data, not real financial advice or a public offer.
