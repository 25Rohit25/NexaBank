'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { ChevronRight, Landmark, Plus } from 'lucide-react';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Account, apiRequest, customerIdFromToken, demoAccounts, formatMoney, getToken } from '@/lib/api';

export function AccountsView() {
  const [accounts, setAccounts] = useState<Account[]>(demoAccounts);
  const [demo, setDemo] = useState(true);

  useEffect(() => {
    const token = getToken(); const customerId = token ? customerIdFromToken(token) : null;
    if (!customerId) return;
    apiRequest<Account[]>(`/api/v1/accounts/customer/${customerId}`)
      .then((result) => { setAccounts(result); setDemo(false); })
      .catch(() => setDemo(true));
  }, []);

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="max-w-xl text-sm leading-6 text-muted-foreground">Balances are read from the account service after ownership is verified from your JWT.</p>
        <Button><Plus /> Open account</Button>
      </div>
      {demo && <div className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">Showing portfolio demo data. Sign in while the backend is running to load live accounts.</div>}
      <div className="grid gap-4 md:grid-cols-2">
        {accounts.map((account) => <Card key={account.accountId} className="shadow-sm ring-border">
          <CardHeader><div className="mb-3 grid size-10 place-items-center rounded-xl bg-accent text-accent-foreground"><Landmark className="size-5" /></div><CardTitle className="capitalize">{account.accountType.toLowerCase()} account</CardTitle><CardDescription>•••• {account.accountId.slice(-4)} · {account.currency}</CardDescription><CardAction><Badge variant="secondary">{account.status}</Badge></CardAction></CardHeader>
          <CardContent><p className="font-heading text-3xl font-semibold">{formatMoney(Number(account.balance), account.currency)}</p><Button render={<Link href={`/accounts/${account.accountId}`} />} variant="ghost" className="mt-5 px-0">Account details <ChevronRight /></Button></CardContent>
        </Card>)}
      </div>
    </div>
  );
}
