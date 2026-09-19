'use client';

import { useEffect, useMemo, useState } from 'react';
import { ArrowDownLeft, ArrowUpRight, LoaderCircle, Search } from 'lucide-react';

import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Account, apiRequest, customerIdFromToken, demoTransactions, formatMoney, getToken, Transaction } from '@/lib/api';

const transactionLabels: Record<Transaction['type'], string> = {
  DEPOSIT: 'Deposit', WITHDRAWAL: 'Withdrawal', TRANSFER_IN: 'Transfer received', TRANSFER_OUT: 'Transfer sent', PAYMENT: 'Payment',
};

export function TransactionsView() {
  const [minimum, setMinimum] = useState('');
  const [items, setItems] = useState<Transaction[]>(demoTransactions);
  const [demo, setDemo] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const token = getToken(); const customerId = token ? customerIdFromToken(token) : null;
    if (!customerId) return;
    void Promise.resolve().then(() => setLoading(true));
    apiRequest<Account[]>(`/api/v1/accounts/customer/${customerId}`)
      .then((accounts) => Promise.all(accounts.map((account) => apiRequest<Transaction[]>(`/api/v1/transactions/account/${account.accountId}`))))
      .then((histories) => { setItems(histories.flat().sort((a, b) => Date.parse(b.occurredAt) - Date.parse(a.occurredAt))); setDemo(false); })
      .catch((reason: Error) => { setDemo(false); setError(reason.message); })
      .finally(() => setLoading(false));
  }, []);

  const transactions = useMemo(() => items.filter((item) => !minimum || item.amount >= Number(minimum)), [items, minimum]);
  return <div className="space-y-5">
    <div className="flex flex-wrap items-center justify-between gap-4 rounded-xl border bg-card p-4">
      <div><p className="font-medium">Transaction history</p><p className="text-sm text-muted-foreground">Filter the read projection by minimum amount.</p></div>
      <div className="relative w-full sm:w-64"><Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" /><Input aria-label="Minimum transaction amount" type="number" min="0" placeholder="Minimum amount" value={minimum} onChange={(event) => setMinimum(event.target.value)} className="pl-9" /></div>
    </div>
    {demo && <div className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">Showing portfolio demo data. Sign in to load your transaction projection.</div>}
    {error && <div role="alert" className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">{error}</div>}
    <div className="overflow-hidden rounded-xl border bg-card">
      {loading && <div className="flex items-center justify-center gap-2 p-10 text-sm text-muted-foreground"><LoaderCircle className="size-4 animate-spin" /> Loading transaction history…</div>}
      {transactions.map((item) => {
        const incoming = item.type === 'DEPOSIT' || item.type === 'TRANSFER_IN';
        const Icon = incoming ? ArrowDownLeft : ArrowUpRight;
        const counterparty = item.counterpartyAccountId ? ` · Account •••• ${item.counterpartyAccountId.slice(-4)}` : '';
        return <div key={item.transactionId} className="flex items-center gap-4 border-b p-4 last:border-b-0"><div className="grid size-10 shrink-0 place-items-center rounded-xl bg-muted"><Icon className="size-4" /></div><div className="min-w-0 flex-1"><p className="truncate font-medium">{transactionLabels[item.type]}</p><p className="truncate text-xs text-muted-foreground">{new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(item.occurredAt))}{counterparty}</p></div><Badge variant="secondary" className="hidden sm:inline-flex">{item.status}</Badge><p className={`shrink-0 font-semibold tabular-nums ${incoming ? 'text-emerald-700' : ''}`}>{incoming ? '+' : '−'}{formatMoney(item.amount, item.currency)}</p></div>;
      })}
      {!loading && !error && !transactions.length && <p className="p-10 text-center text-sm text-muted-foreground">{items.length ? 'No transactions match this amount.' : 'No transactions yet. Deposits and transfers will appear here.'}</p>}
    </div>
  </div>;
}
