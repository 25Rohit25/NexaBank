import { ArrowLeft, ShieldCheck } from 'lucide-react';
import Link from 'next/link';

import { AppShell } from '@/components/nexa/app-shell';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export default async function AccountDetailsPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <AppShell eyebrow="Account details" title={`Account •••• ${id.slice(-4)}`}>
    <Button render={<Link href="/accounts" />} variant="ghost" className="mb-4 px-0"><ArrowLeft /> Back to accounts</Button>
    <div className="grid gap-5 md:grid-cols-[1fr_320px]">
      <Card className="bg-primary text-primary-foreground"><CardHeader><Badge variant="secondary">ACTIVE</Badge><CardTitle className="text-white/70">Available balance</CardTitle></CardHeader><CardContent><p className="font-heading text-4xl font-semibold">₹92,450.00</p><p className="mt-3 text-sm text-white/55">Savings · INR · •••• {id.slice(-4)}</p></CardContent></Card>
      <Card><CardHeader><CardTitle>Account protection</CardTitle></CardHeader><CardContent className="flex gap-3 text-sm leading-6 text-muted-foreground"><ShieldCheck className="mt-1 size-5 shrink-0 text-emerald-600" /> Only the account owner or an authorized administrator can retrieve this record.</CardContent></Card>
    </div>
  </AppShell>;
}
