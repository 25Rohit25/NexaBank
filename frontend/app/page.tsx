import {
  ArrowDownLeft, ArrowUpRight, Bot, Building2, ChevronRight, CircleDollarSign,
  CreditCard, LayoutDashboard, MoreHorizontal, Plus, ReceiptText, Send,
  ShieldCheck, Sparkles, WalletCards,
} from 'lucide-react';

import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle,
} from '@/components/ui/card';
import { Progress, ProgressLabel, ProgressValue } from '@/components/ui/progress';

const navigation = [
  { label: 'Overview', href: '/dashboard', icon: LayoutDashboard, active: true },
  { label: 'Accounts', href: '/accounts', icon: WalletCards },
  { label: 'Transactions', href: '/transactions', icon: ReceiptText },
  { label: 'Cards', href: '/settings', icon: CreditCard },
];

const transactions = [
  { name: 'Riverside Market', note: 'Groceries · Today', amount: '−₹2,480', icon: ArrowUpRight },
  { name: 'Salary credit', note: 'Income · 28 Aug', amount: '+₹86,000', icon: ArrowDownLeft },
  { name: 'Metro utilities', note: 'Bills · 27 Aug', amount: '−₹3,240', icon: ArrowUpRight },
];

export default function Home() {
  return (
    <main className="min-h-screen bg-background text-foreground lg:grid lg:grid-cols-[248px_minmax(0,1fr)]">
      <aside className="hidden border-r border-sidebar-border bg-sidebar px-5 py-6 lg:flex lg:flex-col">
        <div className="flex items-center gap-3 px-2">
          <div className="grid size-10 place-items-center rounded-xl bg-primary text-primary-foreground shadow-sm">
            <Building2 className="size-5" aria-hidden="true" />
          </div>
          <div>
            <p className="font-heading text-lg font-semibold tracking-tight">Nexa Bank</p>
            <p className="text-xs text-muted-foreground">Personal banking</p>
          </div>
        </div>

        <nav className="mt-10 space-y-1" aria-label="Primary navigation">
          {navigation.map(({ label, href, icon: Icon, active }) => (
            <a
              key={label}
              href={href}
              aria-current={active ? 'page' : undefined}
              className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${
                active
                  ? 'bg-sidebar-primary text-sidebar-primary-foreground shadow-sm'
                  : 'text-sidebar-foreground/70 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground'
              }`}
            >
              <Icon className="size-4" aria-hidden="true" />
              {label}
            </a>
          ))}
        </nav>

        <div className="mt-auto rounded-xl border border-sidebar-border bg-sidebar-accent/60 p-4">
          <div className="mb-3 flex items-center gap-2 text-sm font-medium">
            <ShieldCheck className="size-4 text-emerald-600" aria-hidden="true" />
            Protected session
          </div>
          <p className="text-xs leading-5 text-muted-foreground">
            Your identity and banking actions are verified by Nexa&apos;s secure services.
          </p>
        </div>
      </aside>

      <section className="min-w-0">
        <header className="flex h-20 items-center justify-between border-b border-border/70 px-5 sm:px-8 xl:px-10">
          <div className="flex items-center gap-3 lg:hidden">
            <div className="grid size-9 place-items-center rounded-lg bg-primary text-primary-foreground">
              <Building2 className="size-4" aria-hidden="true" />
            </div>
            <span className="font-heading font-semibold">Nexa Bank</span>
          </div>
          <div className="hidden lg:block">
            <p className="text-sm text-muted-foreground">Tuesday, 1 September</p>
            <p className="font-heading text-base font-medium">Good afternoon, Rohit</p>
          </div>
          <div className="flex items-center gap-3">
            <Badge variant="outline" className="hidden border-emerald-200 bg-emerald-50 text-emerald-800 sm:inline-flex">
              <span className="size-1.5 rounded-full bg-emerald-500" /> All systems normal
            </Badge>
            <Avatar size="lg"><AvatarFallback className="bg-primary text-primary-foreground">RS</AvatarFallback></Avatar>
          </div>
        </header>

        <div className="mx-auto grid max-w-[1480px] gap-6 px-5 py-6 sm:px-8 lg:grid-cols-[minmax(0,1fr)_320px] xl:px-10 xl:py-8">
          <div className="min-w-0 space-y-6">
            <section className="overflow-hidden rounded-2xl bg-primary p-6 text-primary-foreground shadow-[0_24px_60px_-32px_rgba(13,40,35,0.7)] sm:p-8">
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                  <p className="text-sm text-primary-foreground/65">Total available balance</p>
                  <p className="mt-2 font-heading text-4xl font-semibold tracking-[-0.04em] sm:text-5xl">₹1,24,850.00</p>
                  <p className="mt-3 text-sm text-primary-foreground/60">Across 2 active accounts</p>
                </div>
                <Button render={<a href="/transfers" />} variant="secondary" size="lg"><Send data-icon="inline-start" /> Transfer money</Button>
              </div>
              <div className="mt-8 grid gap-3 border-t border-white/10 pt-5 sm:grid-cols-3">
                <div><p className="text-xs text-white/50">Money in this month</p><p className="mt-1 font-medium">₹92,500</p></div>
                <div><p className="text-xs text-white/50">Money out this month</p><p className="mt-1 font-medium">₹31,240</p></div>
                <div><p className="text-xs text-white/50">Monthly change</p><p className="mt-1 font-medium text-emerald-300">+12.4%</p></div>
              </div>
            </section>

            <section aria-labelledby="accounts-heading">
              <div className="mb-3 flex items-end justify-between">
                <div><p className="eyebrow">Your money</p><h1 id="accounts-heading" className="font-heading text-2xl font-semibold tracking-tight">Accounts</h1></div>
                <Button render={<a href="/accounts" />} variant="ghost" size="sm">View all <ChevronRight data-icon="inline-end" /></Button>
              </div>
              <div className="grid gap-4 md:grid-cols-2">
                <Card className="border-0 shadow-sm ring-border">
                  <CardHeader>
                    <CardTitle>Savings account</CardTitle><CardDescription>•••• 1001 · Primary</CardDescription>
                    <CardAction><Badge variant="secondary">4.1% p.a.</Badge></CardAction>
                  </CardHeader>
                  <CardContent>
                    <p className="font-heading text-2xl font-semibold">₹92,450.00</p>
                    <Progress value={74} className="mt-5"><ProgressLabel>Monthly savings goal</ProgressLabel><ProgressValue>74%</ProgressValue></Progress>
                  </CardContent>
                </Card>
                <Card className="border-0 shadow-sm ring-border">
                  <CardHeader>
                    <CardTitle>Current account</CardTitle><CardDescription>•••• 2048 · Daily spending</CardDescription>
                    <CardAction><Button variant="ghost" size="icon-sm" aria-label="Current account options"><MoreHorizontal /></Button></CardAction>
                  </CardHeader>
                  <CardContent><p className="font-heading text-2xl font-semibold">₹32,400.00</p><p className="mt-5 text-xs text-muted-foreground">No pending charges</p></CardContent>
                </Card>
              </div>
            </section>

            <Card className="border-0 shadow-sm ring-border">
              <CardHeader><CardTitle>Recent activity</CardTitle><CardDescription>Latest movements across your accounts</CardDescription><CardAction><Button render={<a href="/transactions" />} variant="outline" size="sm">All transactions</Button></CardAction></CardHeader>
              <CardContent className="divide-y divide-border p-0">
                {transactions.map(({ name, note, amount, icon: Icon }) => (
                  <div key={name} className="flex items-center gap-3 px-4 py-3.5">
                    <div className="grid size-9 place-items-center rounded-lg bg-muted text-muted-foreground"><Icon className="size-4" /></div>
                    <div className="min-w-0 flex-1"><p className="truncate text-sm font-medium">{name}</p><p className="text-xs text-muted-foreground">{note}</p></div>
                    <p className={`text-sm font-semibold tabular-nums ${amount.startsWith('+') ? 'text-emerald-700' : ''}`}>{amount}</p>
                  </div>
                ))}
              </CardContent>
            </Card>
          </div>

          <aside className="space-y-6">
            <Card className="border-0 bg-accent/60 shadow-sm ring-border">
              <CardHeader>
                <div className="mb-3 grid size-10 place-items-center rounded-xl bg-primary text-primary-foreground"><Sparkles className="size-5" /></div>
                <CardTitle>Ask Nexa</CardTitle><CardDescription>Your secure assistant can check live accounts and explain bank policies.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-2">
                <Button render={<a href="/ai-assistant" />} variant="outline" className="h-auto w-full justify-between bg-card py-3 text-left whitespace-normal"><span>Can I transfer ₹50,000 internationally?</span><Bot className="size-4" /></Button>
                <Button render={<a href="/ai-assistant" />} variant="outline" className="h-auto w-full justify-between bg-card py-3 text-left whitespace-normal"><span>Show transactions above ₹5,000</span><Bot className="size-4" /></Button>
              </CardContent>
            </Card>

            <section aria-labelledby="quick-actions-heading">
              <p className="eyebrow">Shortcuts</p><h2 id="quick-actions-heading" className="mb-3 font-heading text-xl font-semibold">Quick actions</h2>
              <div className="grid grid-cols-3 gap-2">
                {[{ label: 'Transfer', icon: Send }, { label: 'Deposit', icon: Plus }, { label: 'Pay bill', icon: CircleDollarSign }].map(({ label, icon: Icon }) => (
                  <Button key={label} variant="outline" className="h-20 flex-col gap-2 bg-card text-xs"><Icon className="size-4" />{label}</Button>
                ))}
              </div>
            </section>

            <div className="rounded-xl border border-dashed border-border px-4 py-3 text-xs leading-5 text-muted-foreground">
              <span className="font-medium text-foreground">Demo data.</span> Balances are representative until you sign in to the local Nexa Bank backend.
            </div>
          </aside>
        </div>
      </section>
    </main>
  );
}
