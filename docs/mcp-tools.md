# Banking MCP tools

The MCP server uses Spring AI 2.0.1 Streamable HTTP at `/mcp` (port 8090 directly or port 8080 through the gateway). Every MCP request requires `Authorization: Bearer <customer JWT>`.

| Tool | Inputs | Behavior |
| --- | --- | --- |
| `getCustomerAccounts` | none | Lists accounts for the verified JWT subject; customer ID is never model-supplied |
| `getAccountBalance` | `accountId` | Calls the secured Account API; other customers' accounts return 403 |
| `getTransactions` | `accountId`, optional `from`, `to`, `minAmount` | Calls the secured Transaction API |
| `prepareTransfer` | source, destination, amount | Validates owned accounts and funds, then stores an immutable five-minute Redis confirmation |
| `executeTransfer` | `confirmationToken` | Executes only the stored transfer details after explicit user confirmation |

The MCP server does not access PostgreSQL. It relays the authenticated JWT to deterministic services through the gateway. `executeTransfer` uses the confirmation token as `Idempotency-Key`; it cannot accept replacement account IDs or amounts.

## Security invariants

- `/mcp` is authenticated; the Spring AI transport itself does not provide authentication.
- Identity comes from the verified JWT subject.
- Backend owner/admin checks remain authoritative.
- Preparing a transfer never moves money.
- A confirmation belongs to one customer and expires after five minutes.
- A successful execution deletes the confirmation; backend idempotency protects retry races.
