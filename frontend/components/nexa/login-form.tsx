'use client';

import { useState } from 'react';
import type { SyntheticEvent } from 'react';
import { ArrowRight, Building2, ShieldCheck } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { apiRequest, saveToken } from '@/lib/api';

export function LoginForm() {
  const [email, setEmail] = useState('rohit@example.com');
  const [password, setPassword] = useState('securePassword');
  const [error, setError] = useState('');
  const [pending, setPending] = useState(false);

  async function submit(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault(); setPending(true); setError('');
    try {
      const result = await apiRequest<{ accessToken: string }>('/api/v1/auth/login', {
        method: 'POST', body: JSON.stringify({ email, password }),
      });
      saveToken(result.accessToken); window.location.assign('/dashboard');
    } catch {
      setError('Could not sign in. Start the Nexa Bank backend and check your credentials.');
    } finally { setPending(false); }
  }

  return (
    <main className="grid min-h-screen bg-primary lg:grid-cols-[1.1fr_0.9fr]">
      <section className="hidden flex-col justify-between p-12 text-primary-foreground lg:flex xl:p-16">
        <div className="flex items-center gap-3"><div className="grid size-11 place-items-center rounded-xl bg-white/10"><Building2 /></div><span className="font-heading text-xl font-semibold">Nexa Bank</span></div>
        <div className="max-w-xl"><p className="eyebrow text-emerald-300">Banking, with clarity</p><h1 className="font-heading text-5xl font-semibold leading-[1.05] tracking-[-0.04em] xl:text-6xl">Your money.<br />Clearly in view.</h1><p className="mt-6 max-w-md text-base leading-7 text-white/60">Secure accounts, deterministic transfers, and an AI assistant grounded in your live banking data and Nexa policies.</p></div>
        <div className="flex items-center gap-2 text-sm text-white/60"><ShieldCheck className="size-4 text-emerald-300" /> Protected by JWT identity and account-level authorization</div>
      </section>
      <section className="flex items-center justify-center rounded-t-[2rem] bg-background p-6 lg:rounded-l-[2rem] lg:rounded-tr-none">
        <form onSubmit={submit} className="w-full max-w-sm">
          <div className="mb-8 lg:hidden"><div className="mb-8 flex items-center gap-2"><Building2 className="size-5" /><span className="font-heading font-semibold">Nexa Bank</span></div></div>
          <p className="eyebrow">Welcome back</p><h2 className="font-heading text-3xl font-semibold tracking-tight">Sign in to Nexa</h2><p className="mt-2 text-sm text-muted-foreground">Use the customer credentials registered with your local backend.</p>
          <div className="mt-8 space-y-5">
            <div className="space-y-2"><Label htmlFor="email">Email address</Label><Input id="email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required /></div>
            <div className="space-y-2"><Label htmlFor="password">Password</Label><Input id="password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} required minLength={12} /></div>
          </div>
          {error && <p role="alert" className="mt-4 rounded-lg bg-destructive/10 p-3 text-sm text-destructive">{error}</p>}
          <Button type="submit" size="lg" className="mt-6 w-full" disabled={pending}>{pending ? 'Signing in…' : 'Sign in'} {!pending && <ArrowRight data-icon="inline-end" />}</Button>
          <p className="mt-5 text-center text-xs leading-5 text-muted-foreground">The access token is kept only in this browser tab and cleared when the session ends.</p>
        </form>
      </section>
    </main>
  );
}
