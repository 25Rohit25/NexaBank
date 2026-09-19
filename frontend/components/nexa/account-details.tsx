'use client';

import { useEffect, useState } from 'react';
import { LoaderCircle, Plus, ShieldCheck } from 'lucide-react';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Account, apiRequest, formatMoney } from '@/lib/api';

export function AccountDetails({ accountId }: { accountId: string }) {
  const [account, setAccount] = useState<Account | null>(null);
  const [error, setError] = useState('');
  const [depositAmount, setDepositAmount] = useState(''); const [depositing, setDepositing] = useState(false); const [message, setMessage] = useState('');

  useEffect(() => {
    apiRequest<Account>(`/api/v1/accounts/${accountId}`)
      .then(setAccount)
      .catch((reason: Error) => setError(reason.message));
  }, [accountId]);

  async function deposit() {
    if (!account) return;
    const amount = Number(depositAmount);
    if (!Number.isFinite(amount) || amount <= 0) { setMessage('Enter an amount greater than zero.'); return; }
    setDepositing(true); setMessage('');
    try {
      const result = await apiRequest<{ balance: number }>(`/api/v1/accounts/${accountId}/deposits`, { method: 'POST', headers: { 'Idempotency-Key': crypto.randomUUID() }, body: JSON.stringify({ amount }) });
      setAccount({ ...account, balance: result.balance }); setDepositAmount(''); setMessage('Deposit completed.');
    } catch (reason) { setMessage(reason instanceof Error ? reason.message : 'Deposit could not be completed.'); }
    finally { setDepositing(false); }
  }

  if (error) return <div role="alert" className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">{error} Sign in with the account owner and retry.</div>;
  if (!account) return <div className="flex items-center gap-2 rounded-xl border bg-card p-6 text-sm text-muted-foreground"><LoaderCircle className="size-4 animate-spin" /> Loading account details…</div>;

  return <div className="grid gap-5 md:grid-cols-[1fr_320px]">
    <Card className="bg-primary text-primary-foreground"><CardHeader><Badge variant="secondary">{account.status}</Badge><CardTitle className="text-white/70">Available balance</CardTitle></CardHeader><CardContent><p className="font-heading text-4xl font-semibold">{formatMoney(Number(account.balance), account.currency)}</p><p className="mt-3 text-sm text-white/55 capitalize">{account.type.toLowerCase()} · {account.currency} · •••• {account.accountNumber.slice(-4)}</p></CardContent></Card>
    <div className="space-y-5"><Card><CardHeader><CardTitle>Add demo funds</CardTitle></CardHeader><CardContent><form className="space-y-3" onSubmit={(event) => { event.preventDefault(); void deposit(); }}><div className="space-y-2"><Label htmlFor="deposit-amount">Amount ({account.currency})</Label><Input id="deposit-amount" type="number" min="0.01" step="0.01" value={depositAmount} onChange={(event) => setDepositAmount(event.target.value)} required /></div><Button type="submit" disabled={depositing}>{depositing ? <LoaderCircle className="animate-spin" /> : <Plus />}{depositing ? 'Depositing…' : 'Deposit'}</Button></form>{message && <output className="mt-3 block rounded-lg bg-muted p-3 text-sm text-foreground" aria-live="polite">{message}</output>}</CardContent></Card><Card><CardHeader><CardTitle>Account protection</CardTitle></CardHeader><CardContent className="flex gap-3 text-sm leading-6 text-muted-foreground"><ShieldCheck className="mt-1 size-5 shrink-0 text-emerald-600" /> Only the account owner or an authorized administrator can retrieve this record.</CardContent></Card></div>
  </div>;
}
