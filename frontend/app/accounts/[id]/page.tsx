import { ArrowLeft } from 'lucide-react';
import Link from 'next/link';

import { AccountDetails } from '@/components/nexa/account-details';
import { AppShell } from '@/components/nexa/app-shell';
import { Button } from '@/components/ui/button';

export default async function AccountDetailsPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <AppShell eyebrow="Account details" title={`Account •••• ${id.slice(-4)}`}>
    <Button render={<Link href="/accounts" />} variant="ghost" className="mb-4 px-0"><ArrowLeft /> Back to accounts</Button>
    <AccountDetails accountId={id} />
  </AppShell>;
}
