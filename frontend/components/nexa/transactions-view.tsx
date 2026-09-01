'use client';

import { useMemo, useState } from 'react';
import { ArrowDownLeft, ArrowUpRight, Search } from 'lucide-react';

import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { demoTransactions, formatMoney } from '@/lib/api';

export function TransactionsView() {
  const [minimum, setMinimum] = useState('');
  const transactions = useMemo(() => demoTransactions.filter((item) => !minimum || item.amount >= Number(minimum)), [minimum]);
  return <div className="space-y-5">
    <div className="flex flex-wrap items-center justify-between gap-4 rounded-xl border bg-card p-4">
      <div><p className="font-medium">Transaction history</p><p className="text-sm text-muted-foreground">Filter the read projection by minimum amount.</p></div>
      <div className="relative w-full sm:w-64"><Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" /><Input aria-label="Minimum transaction amount" type="number" min="0" placeholder="Minimum amount" value={minimum} onChange={(event) => setMinimum(event.target.value)} className="pl-9" /></div>
    </div>
    <div className="overflow-hidden rounded-xl border bg-card">
      {transactions.map((item) => {
        const incoming = item.transactionType === 'DEPOSIT' || item.transactionType === 'TRANSFER_IN';
        const Icon = incoming ? ArrowDownLeft : ArrowUpRight;
        return <div key={item.transactionId} className="flex items-center gap-4 border-b p-4 last:border-b-0"><div className="grid size-10 place-items-center rounded-xl bg-muted"><Icon className="size-4" /></div><div className="min-w-0 flex-1"><p className="truncate font-medium">{item.description}</p><p className="text-xs text-muted-foreground">{item.category} · {new Date(item.createdAt).toLocaleDateString('en-IN')}</p></div><Badge variant="secondary" className="hidden sm:inline-flex">{item.status}</Badge><p className={`font-semibold tabular-nums ${incoming ? 'text-emerald-700' : ''}`}>{incoming ? '+' : '−'}{formatMoney(item.amount, item.currency)}</p></div>;
      })}
      {!transactions.length && <p className="p-10 text-center text-sm text-muted-foreground">No transactions match this amount.</p>}
    </div>
  </div>;
}
