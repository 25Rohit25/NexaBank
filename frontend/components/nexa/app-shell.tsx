'use client';

import { usePathname } from 'next/navigation';
import {
  Bot, Building2, LayoutDashboard, LogOut, ReceiptText,
  Send, Settings, ShieldCheck, WalletCards,
} from 'lucide-react';
import type { ReactNode } from 'react';

import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { clearToken } from '@/lib/api';

const navigation = [
  { href: '/dashboard', label: 'Overview', icon: LayoutDashboard },
  { href: '/accounts', label: 'Accounts', icon: WalletCards },
  { href: '/transactions', label: 'Transactions', icon: ReceiptText },
  { href: '/transfers', label: 'Transfers', icon: Send },
  { href: '/ai-assistant', label: 'Ask Nexa', icon: Bot },
  { href: '/settings', label: 'Settings', icon: Settings },
];

export function AppShell({ title, eyebrow, children }: { title: string; eyebrow: string; children: ReactNode }) {
  const pathname = usePathname();

  return (
    <main className="min-h-screen bg-background text-foreground lg:grid lg:grid-cols-[248px_minmax(0,1fr)]">
      <aside className="hidden border-r border-sidebar-border bg-sidebar px-5 py-6 lg:flex lg:flex-col">
        <a href="/dashboard" className="flex items-center gap-3 px-2">
          <div className="grid size-10 place-items-center rounded-xl bg-primary text-primary-foreground"><Building2 className="size-5" /></div>
          <div><p className="font-heading text-lg font-semibold tracking-tight">Nexa Bank</p><p className="text-xs text-muted-foreground">Personal banking</p></div>
        </a>
        <nav className="mt-10 space-y-1" aria-label="Primary navigation">
          {navigation.map(({ href, label, icon: Icon }) => {
            const active = pathname === href || pathname.startsWith(`${href}/`);
            return <a key={href} href={href} aria-current={active ? 'page' : undefined} className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${active ? 'bg-sidebar-primary text-sidebar-primary-foreground shadow-sm' : 'text-sidebar-foreground/70 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground'}`}><Icon className="size-4" />{label}</a>;
          })}
        </nav>
        <div className="mt-auto space-y-3">
          <div className="rounded-xl border border-sidebar-border bg-sidebar-accent/60 p-4"><div className="mb-2 flex items-center gap-2 text-sm font-medium"><ShieldCheck className="size-4 text-emerald-600" />Protected session</div><p className="text-xs leading-5 text-muted-foreground">JWT identity and account ownership are verified by the backend.</p></div>
          <Button variant="ghost" className="w-full justify-start" onClick={() => { clearToken(); window.location.assign('/login'); }}><LogOut /> Sign out</Button>
        </div>
      </aside>
      <section className="min-w-0">
        <header className="flex h-20 items-center justify-between border-b border-border/70 px-5 sm:px-8 xl:px-10">
          <div><p className="eyebrow">{eyebrow}</p><h1 className="font-heading text-xl font-semibold tracking-tight">{title}</h1></div>
          <div className="flex items-center gap-3"><Badge variant="outline" className="hidden border-emerald-200 bg-emerald-50 text-emerald-800 sm:inline-flex"><span className="size-1.5 rounded-full bg-emerald-500" /> Secure</Badge><Avatar size="lg"><AvatarFallback className="bg-primary text-primary-foreground">RS</AvatarFallback></Avatar></div>
        </header>
        <div className="mx-auto max-w-[1240px] px-5 py-6 sm:px-8 xl:px-10 xl:py-8">{children}</div>
      </section>
    </main>
  );
}
