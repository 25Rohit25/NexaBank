'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { ChevronRight, Landmark, LoaderCircle, Plus, X } from 'lucide-react';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Account, apiRequest, customerIdFromToken, demoAccounts, formatMoney, getToken } from '@/lib/api';

export function AccountsView() {
  const [accounts, setAccounts] = useState<Account[]>(demoAccounts);
  const [demo, setDemo] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [creating, setCreating] = useState(false);
  const [showCreate, setShowCreate] = useState(false);
  const [accountType, setAccountType] = useState<Account['type']>('SAVINGS');

  useEffect(() => {
    const token = getToken(); const customerId = token ? customerIdFromToken(token) : null;
    if (!customerId) return;
    void Promise.resolve().then(() => setLoading(true));
    apiRequest<Account[]>(`/api/v1/accounts/customer/${customerId}`)
      .then((result) => { setAccounts(result); setDemo(false); })
      .catch((reason: Error) => { setDemo(false); setError(reason.message); })
      .finally(() => setLoading(false));
  }, []);

  async function createAccount() {
    setCreating(true); setError('');
    try {
      const created = await apiRequest<Account>('/api/v1/accounts', {
        method: 'POST', body: JSON.stringify({ accountType, currency: 'INR' }),
      });
      setAccounts((current) => [...current, created]); setShowCreate(false);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'The account could not be opened. Please retry.');
    } finally { setCreating(false); }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="max-w-xl text-sm leading-6 text-muted-foreground">Balances are read from the account service after ownership is verified from your JWT.</p>
        {demo ? <Button render={<Link href="/login" />}><Plus /> Sign in to open an account</Button> : <Button onClick={() => setShowCreate((value) => !value)}><Plus /> Open account</Button>}
      </div>
      {demo && <div className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">Showing portfolio demo data. Sign in while the backend is running to load live accounts.</div>}
      {error && <div role="alert" className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">{error}</div>}
      {showCreate && <div className="flex flex-wrap items-end gap-3 rounded-xl border bg-card p-4">
        <label className="grid gap-2 text-sm font-medium">Account type
          <select className="h-9 rounded-lg border border-input bg-transparent px-3 text-sm outline-none focus-visible:ring-3 focus-visible:ring-ring/50" value={accountType} onChange={(event) => setAccountType(event.target.value as Account['type'])}>
            <option value="SAVINGS">Savings</option><option value="CURRENT">Current</option>
          </select>
        </label>
        <p className="min-w-40 flex-1 pb-2 text-sm text-muted-foreground">New accounts use INR. Each customer may hold one account of each type.</p>
        <Button onClick={() => void createAccount()} disabled={creating}>{creating ? <LoaderCircle className="animate-spin" /> : <Plus />} {creating ? 'Opening…' : 'Open account'}</Button>
        <Button variant="ghost" size="icon" aria-label="Cancel opening account" onClick={() => setShowCreate(false)} disabled={creating}><X /></Button>
      </div>}
      {loading && <div className="flex items-center gap-2 rounded-xl border bg-card p-6 text-sm text-muted-foreground"><LoaderCircle className="size-4 animate-spin" /> Loading your accounts…</div>}
      <div className="grid gap-4 md:grid-cols-2">
        {accounts.map((account) => <Card key={account.accountId} className="shadow-sm ring-border">
          <CardHeader><div className="mb-3 grid size-10 place-items-center rounded-xl bg-accent text-accent-foreground"><Landmark className="size-5" /></div><CardTitle className="capitalize">{account.type.toLowerCase()} account</CardTitle><CardDescription>•••• {account.accountNumber.slice(-4)} · {account.currency}</CardDescription><CardAction><Badge variant="secondary">{account.status}</Badge></CardAction></CardHeader>
          <CardContent><p className="font-heading text-3xl font-semibold">{formatMoney(Number(account.balance), account.currency)}</p><Button render={<Link href={`/accounts/${account.accountId}`} />} variant="ghost" className="mt-5 px-0">Account details <ChevronRight /></Button></CardContent>
        </Card>)}
      </div>
      {!loading && !demo && !error && accounts.length === 0 && <div className="rounded-xl border bg-card p-8 text-center"><p className="font-medium">No accounts yet</p><p className="mt-1 text-sm text-muted-foreground">Open a savings or current account to get started.</p></div>}
    </div>
  );
}
