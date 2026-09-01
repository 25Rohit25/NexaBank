'use client';

import { useState } from 'react';
import { ArrowRight, CheckCircle2, ShieldCheck } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { apiRequest, formatMoney } from '@/lib/api';

type Preview = { source: string; destination: string; amount: number };

export function TransferForm() {
  const [source, setSource] = useState('ACC-DEMO-1001'); const [destination, setDestination] = useState('ACC-DEMO-2048'); const [amount, setAmount] = useState('5000');
  const [preview, setPreview] = useState<Preview | null>(null); const [status, setStatus] = useState(''); const [pending, setPending] = useState(false);

  async function execute() {
    if (!preview) return; setPending(true); setStatus('');
    try {
      const result = await apiRequest<{ transferId: string; status: string }>('/api/v1/transfers', { method: 'POST', headers: { 'Idempotency-Key': crypto.randomUUID() }, body: JSON.stringify({ sourceAccountId: preview.source, destinationAccountId: preview.destination, amount: preview.amount }) });
      setStatus(`Transfer ${result.status.toLowerCase()} · ${result.transferId}`); setPreview(null);
    } catch { setStatus('Transfer could not be verified. Sign in and start the banking backend before retrying.'); }
    finally { setPending(false); }
  }

  return <div className="grid gap-5 lg:grid-cols-[1fr_360px]">
    <Card><CardHeader><CardTitle>Transfer between accounts</CardTitle></CardHeader><CardContent>
      <form className="space-y-5" onSubmit={(event) => { event.preventDefault(); setPreview({ source, destination, amount: Number(amount) }); setStatus(''); }}>
        <div className="space-y-2"><Label htmlFor="source">From account</Label><Input id="source" value={source} onChange={(event) => setSource(event.target.value)} required /></div>
        <div className="space-y-2"><Label htmlFor="destination">Destination account</Label><Input id="destination" value={destination} onChange={(event) => setDestination(event.target.value)} required /></div>
        <div className="space-y-2"><Label htmlFor="amount">Amount (INR)</Label><Input id="amount" type="number" min="0.01" step="0.01" value={amount} onChange={(event) => setAmount(event.target.value)} required /></div>
        <Button type="submit">Review transfer <ArrowRight /></Button>
      </form>
    </CardContent></Card>
    <Card className="bg-accent/50"><CardHeader><CardTitle>{preview ? 'Confirm transfer' : 'Confirmation required'}</CardTitle></CardHeader><CardContent>
      {preview ? <div className="space-y-4"><div className="rounded-xl bg-card p-4"><p className="text-xs text-muted-foreground">Amount</p><p className="mt-1 font-heading text-3xl font-semibold">{formatMoney(preview.amount)}</p><p className="mt-4 text-xs text-muted-foreground">{preview.source} → {preview.destination}</p></div><Button className="w-full" onClick={execute} disabled={pending}>{pending ? 'Verifying…' : 'Confirm and transfer'} <CheckCircle2 /></Button><Button variant="ghost" className="w-full" onClick={() => setPreview(null)}>Cancel</Button></div> : <div className="flex gap-3 text-sm leading-6 text-muted-foreground"><ShieldCheck className="mt-1 size-5 shrink-0 text-emerald-600" /> Nexa never moves money from the first request. Review the source, destination, and amount before explicitly confirming.</div>}
      {status && <output className="mt-4 block rounded-lg border bg-card p-3 text-sm">{status}</output>}
    </CardContent></Card>
  </div>;
}
