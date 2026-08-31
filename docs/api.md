# Banking API

All JSON endpoints use `/api/v1` and are exposed through the gateway at port 8080. Protected endpoints require `Authorization: Bearer <token>`. Money-moving requests also require a unique `Idempotency-Key`; clients may supply `X-Correlation-ID` or accept the gateway-generated value.

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/register` | Public | Create a customer login and return a JWT |
| POST | `/api/v1/auth/login` | Public | Authenticate and return a JWT |
| POST | `/api/v1/customers` | Admin | Create a customer without login credentials |
| GET | `/api/v1/customers/{id}` | Owner/Admin | Get a customer |
| GET | `/api/v1/customers/email/{email}` | Owner/Admin | Find a customer by email |
| PUT | `/api/v1/customers/{id}` | Owner/Admin | Update name and phone |
| POST | `/api/v1/accounts` | Customer/Admin | Create an account for the JWT subject |
| GET | `/api/v1/accounts/{id}` | Owner/Admin | Get an account |
| GET | `/api/v1/accounts/customer/{customerId}` | Owner/Admin | List customer accounts |
| GET | `/api/v1/accounts/{id}/balance` | Owner/Admin | Get current balance |
| POST | `/api/v1/accounts/{id}/deposits` | Owner/Admin | Add demo funds and emit a transaction event |
| POST | `/api/v1/transfers` | Source owner/Admin | Atomically transfer funds between accounts |
| GET | `/api/v1/transactions/{id}` | Owner/Admin | Get a projected transaction |
| GET | `/api/v1/transactions/account/{accountId}` | Owner/Admin | List history; optional `from`, `to`, `minAmount` |

Registration requires a password of 12–72 characters. Phone numbers accept an optional leading `+` and 8–15 digits. Accounts support `SAVINGS` and `CURRENT`; currency is a three-letter uppercase code such as `INR`.

Deposit body: `{"amount":1000.00}`. Transfer body: `{"sourceAccountId":"...","destinationAccountId":"...","amount":25.00}`. Reusing an idempotency key with the same request replays its response; reuse with different request data returns `409 Conflict`.
