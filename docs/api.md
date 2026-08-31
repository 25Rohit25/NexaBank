# Foundation API

All JSON endpoints use the `/api/v1` prefix. Protected endpoints require `Authorization: Bearer <token>`.

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

Registration requires a password of 12–72 characters. Phone numbers accept an optional leading `+` and 8–15 digits. Accounts support `SAVINGS` and `CURRENT`; currency is a three-letter uppercase code such as `INR`.

