'use client';

import { useEffect, useState } from 'react';
import { ArrowRight, CheckCircle2, LoaderCircle, ShieldCheck } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Account, apiRequest, customerIdFromToken, demoAccounts, formatMoney, getToken } from '@/lib/api';

type Preview = { source: string; destination: string; amount: number };

export function TransferForm() {
  const [accounts, setAccounts] = useState<Account[]>(demoAccounts);
  const [source, setSource] = useState(demoAccounts[0].accountId); const [destination, setDestination] = useState(demoAccounts[1].accountId); const [amount, setAmount] = useState('5000');
  const [preview, setPreview] = useState<Preview | null>(null); const [status, setStatus] = useState(''); const [pending, setPending] = useState(false);
  const [demo, setDemo] = useState(true); const [loading, setLoading] = useState(false);

  useEffect(() => {
    const token = getToken(); const customerId = token ? customerIdFromToken(token) : null;
    if (!customerId) return;
    void Promise.resolve().then(() => setLoading(true));
    apiRequest<Account[]>(`/api/v1/accounts/customer/${customerId}`)
      .then((result) => {
        setAccounts(result); setDemo(false);
        setSource(result[0]?.accountId ?? ''); setDestination(result[1]?.accountId ?? '');
      })
      .catch((reason: Error) => { setDemo(false); setStatus(reason.message); })
      .finally(() => setLoading(false));
  }, []);

  async function execute() {
    if (!preview) return; setPending(true); setStatus('');
    try {
      const result = await apiRequest<{ transferId: string; status: string }>('/api/v1/transfers', { method: 'POST', headers: { 'Idempotency-Key': crypto.randomUUID() }, body: JSON.stringify({ sourceAccountId: preview.source, destinationAccountId: preview.destination, amount: preview.amount }) });
      setStatus(`Transfer ${result.status.toLowerCase()} · ${result.transferId}`); setPreview(null);
    } catch { setStatus('Transfer could not be verified. Sign in and start the banking backend before retrying.'); }
    finally { setPending(false); }
  }

  function review() {
    const numericAmount = Number(amount);
    if (!source || !destination) { setStatus('Choose both a source and destination account.'); return; }
    if (source === destination) { setStatus('Choose two different accounts for this transfer.'); return; }
    if (!Number.isFinite(numericAmount) || numericAmount <= 0) { setStatus('Enter an amount greater than zero.'); return; }
    setPreview({ source, destination, amount: numericAmount }); setStatus('');
  }

  const accountLabel = (account: Account) => `${account.type === 'SAVINGS' ? 'Savings' : 'Current'} •••• ${account.accountNumber.slice(-4)} · ${formatMoney(Number(account.balance), account.currency)}`;

  return <div className="grid gap-5 lg:grid-cols-[1fr_360px]">
    <Card><CardHeader><CardTitle>Transfer between accounts</CardTitle></CardHeader><CardContent>
      {demo && <div className="mb-5 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">Demo account choices are shown. Sign in to transfer between your live accounts.</div>}
      <form className="space-y-5" onSubmit={(event) => { event.preventDefault(); review(); }}>
        <div className="space-y-2"><Label htmlFor="source">From account</Label><select id="source" className="h-10 w-full rounded-lg border border-input bg-transparent px-3 text-base outline-none focus-visible:ring-3 focus-visible:ring-ring/50" value={source} onChange={(event) => setSource(event.target.value)} required disabled={loading}>{accounts.map((account) => <option key={account.accountId} value={account.accountId}>{accountLabel(account)}</option>)}</select></div>
        <div className="space-y-2"><Label htmlFor="destination">Destination account</Label><select id="destination" className="h-10 w-full rounded-lg border border-input bg-transparent px-3 text-base outline-none focus-visible:ring-3 focus-visible:ring-ring/50" value={destination} onChange={(event) => setDestination(event.target.value)} required disabled={loading}>{accounts.map((account) => <option key={account.accountId} value={account.accountId}>{accountLabel(account)}</option>)}</select></div>
        <div className="space-y-2"><Label htmlFor="amount">Amount (INR)</Label><input id="amount" className="h-10 w-full rounded-lg border border-input bg-transparent px-3 text-base outline-none focus-visible:ring-3 focus-visible:ring-ring/50" type="number" min="0.01" step="0.01" value={amount} onChange={(event) => setAmount(event.target.value)} required /></div>
        <Button type="submit" disabled={loading || accounts.length < 2}>{loading ? <LoaderCircle className="animate-spin" /> : null}{loading ? 'Loading accounts…' : accounts.length < 2 ? 'Two accounts required' : 'Review transfer'} {!loading && accounts.length >= 2 && <ArrowRight />}</Button>
      </form>
    </CardContent></Card>
    <Card className="bg-accent/50"><CardHeader><CardTitle>{preview ? 'Confirm transfer' : 'Confirmation required'}</CardTitle></CardHeader><CardContent>
      {preview ? <div className="space-y-4"><div className="rounded-xl bg-card p-4"><p className="text-xs text-muted-foreground">Amount</p><p className="mt-1 font-heading text-3xl font-semibold">{formatMoney(preview.amount)}</p><p className="mt-4 text-xs text-muted-foreground">{preview.source} → {preview.destination}</p></div><Button className="w-full" onClick={execute} disabled={pending}>{pending ? 'Verifying…' : 'Confirm and transfer'} <CheckCircle2 /></Button><Button variant="ghost" className="w-full" onClick={() => setPreview(null)}>Cancel</Button></div> : <div className="flex gap-3 text-sm leading-6 text-muted-foreground"><ShieldCheck className="mt-1 size-5 shrink-0 text-emerald-600" /> Nexa never moves money from the first request. Review the source, destination, and amount before explicitly confirming.</div>}
      {status && <output className="mt-4 block rounded-lg border bg-card p-3 text-sm">{status}</output>}
    </CardContent></Card>
  </div>;
}
