'use client';

import { useEffect, useState } from 'react';
import { ArrowDownLeft, ArrowUpRight, Bot, ChevronRight, Landmark, LoaderCircle, Send } from 'lucide-react';
import Link from 'next/link';

import { AppShell } from '@/components/nexa/app-shell';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Account, apiRequest, customerIdFromToken, demoAccounts, demoTransactions, formatMoney, getToken, Transaction } from '@/lib/api';

const transactionLabels: Record<Transaction['type'], string> = {
  DEPOSIT: 'Deposit', WITHDRAWAL: 'Withdrawal', TRANSFER_IN: 'Transfer received', TRANSFER_OUT: 'Transfer sent', PAYMENT: 'Payment',
};

export function DashboardView() {
  const [accounts, setAccounts] = useState<Account[]>(demoAccounts);
  const [transactions, setTransactions] = useState<Transaction[]>(demoTransactions);
  const [demo, setDemo] = useState(true); const [loading, setLoading] = useState(false); const [error, setError] = useState('');

  useEffect(() => {
    const token = getToken(); const customerId = token ? customerIdFromToken(token) : null;
    if (!customerId) return;
    void Promise.resolve().then(() => setLoading(true));
    apiRequest<Account[]>(`/api/v1/accounts/customer/${customerId}`)
      .then(async (liveAccounts) => {
        const histories = await Promise.all(liveAccounts.map((account) => apiRequest<Transaction[]>(`/api/v1/transactions/account/${account.accountId}`)));
        setAccounts(liveAccounts); setTransactions(histories.flat().sort((a, b) => Date.parse(b.occurredAt) - Date.parse(a.occurredAt))); setDemo(false);
      })
      .catch((reason: Error) => { setDemo(false); setError(reason.message); })
      .finally(() => setLoading(false));
  }, []);

  const total = accounts.reduce((sum, account) => sum + Number(account.balance), 0);
  const currency = accounts[0]?.currency ?? 'INR';
  const monthStart = new Date(); monthStart.setDate(1); monthStart.setHours(0, 0, 0, 0);
  const monthly = transactions.filter((item) => new Date(item.occurredAt) >= monthStart);
  const moneyIn = monthly.filter((item) => item.type === 'DEPOSIT' || item.type === 'TRANSFER_IN').reduce((sum, item) => sum + Number(item.amount), 0);
  const moneyOut = monthly.filter((item) => item.type === 'WITHDRAWAL' || item.type === 'PAYMENT' || item.type === 'TRANSFER_OUT').reduce((sum, item) => sum + Number(item.amount), 0);

  return <AppShell eyebrow="Your money" title="Overview"><div className="space-y-6">
    {demo && <div className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">Showing portfolio demo data. Sign in to load your live dashboard.</div>}
    {error && <div role="alert" className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">{error}</div>}
    {loading && <div className="flex items-center gap-2 text-sm text-muted-foreground"><LoaderCircle className="size-4 animate-spin" /> Refreshing verified balances…</div>}
    <section className="overflow-hidden rounded-2xl bg-primary p-6 text-primary-foreground shadow-[0_24px_60px_-32px_rgba(13,40,35,0.7)] sm:p-8">
      <div className="flex flex-wrap items-start justify-between gap-4"><div><p className="text-sm text-primary-foreground/65">Total available balance</p><p className="mt-2 font-heading text-4xl font-semibold tracking-[-0.04em] sm:text-5xl">{formatMoney(total, currency)}</p><p className="mt-3 text-sm text-primary-foreground/60">Across {accounts.length} active {accounts.length === 1 ? 'account' : 'accounts'}</p></div><Button render={<Link href="/transfers" />} variant="secondary" size="lg"><Send /> Transfer money</Button></div>
      <div className="mt-8 grid gap-3 border-t border-white/10 pt-5 sm:grid-cols-2"><div><p className="text-xs text-white/50">Money in this month</p><p className="mt-1 font-medium">{formatMoney(moneyIn, currency)}</p></div><div><p className="text-xs text-white/50">Money out this month</p><p className="mt-1 font-medium">{formatMoney(moneyOut, currency)}</p></div></div>
    </section>
    <section><div className="mb-3 flex items-end justify-between"><h2 className="font-heading text-2xl font-semibold tracking-tight">Accounts</h2><Button render={<Link href="/accounts" />} variant="ghost" size="sm">View all <ChevronRight /></Button></div><div className="grid gap-4 md:grid-cols-2">{accounts.slice(0, 2).map((account) => <Card key={account.accountId} className="shadow-sm ring-border"><CardHeader><div className="mb-2 grid size-9 place-items-center rounded-lg bg-accent"><Landmark className="size-4" /></div><CardTitle className="capitalize">{account.type.toLowerCase()} account</CardTitle><CardDescription>•••• {account.accountNumber.slice(-4)}</CardDescription><CardAction><Badge variant="secondary">{account.status}</Badge></CardAction></CardHeader><CardContent><p className="font-heading text-2xl font-semibold">{formatMoney(Number(account.balance), account.currency)}</p></CardContent></Card>)}{!accounts.length && <Card><CardContent className="p-8 text-center text-sm text-muted-foreground">No accounts yet. Open one from the Accounts page.</CardContent></Card>}</div></section>
    <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_320px]"><Card><CardHeader><CardTitle>Recent activity</CardTitle><CardDescription>Latest movements across your accounts</CardDescription><CardAction><Button render={<Link href="/transactions" />} variant="outline" size="sm">All transactions</Button></CardAction></CardHeader><CardContent className="divide-y divide-border p-0">{transactions.slice(0, 5).map((item) => { const incoming = item.type === 'DEPOSIT' || item.type === 'TRANSFER_IN'; const Icon = incoming ? ArrowDownLeft : ArrowUpRight; return <div key={item.transactionId} className="flex items-center gap-3 px-4 py-3.5"><div className="grid size-9 place-items-center rounded-lg bg-muted text-muted-foreground"><Icon className="size-4" /></div><div className="min-w-0 flex-1"><p className="truncate text-sm font-medium">{transactionLabels[item.type]}</p><p className="text-xs text-muted-foreground">{new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium' }).format(new Date(item.occurredAt))}</p></div><p className={`text-sm font-semibold tabular-nums ${incoming ? 'text-emerald-700' : ''}`}>{incoming ? '+' : '−'}{formatMoney(Number(item.amount), item.currency)}</p></div>; })}{!transactions.length && <p className="p-8 text-center text-sm text-muted-foreground">No transactions yet.</p>}</CardContent></Card><Card className="bg-accent/60"><CardHeader><div className="mb-3 grid size-10 place-items-center rounded-xl bg-primary text-primary-foreground"><Bot className="size-5" /></div><CardTitle>Ask Nexa</CardTitle><CardDescription>Check live accounts or ask about bank policies through the grounded assistant.</CardDescription></CardHeader><CardContent><Button render={<Link href="/ai-assistant" />} className="w-full">Open assistant <ChevronRight /></Button></CardContent></Card></div>
  </div></AppShell>;
}
