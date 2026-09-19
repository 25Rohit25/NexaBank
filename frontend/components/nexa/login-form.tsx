'use client';

import { useState } from 'react';
import type { SyntheticEvent } from 'react';
import { ArrowRight, Building2, ShieldCheck } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { apiRequest, saveToken } from '@/lib/api';

export function LoginForm() {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [pending, setPending] = useState(false);

  async function submit(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault(); setPending(true); setError('');
    try {
      const path = mode === 'login' ? '/api/v1/auth/login' : '/api/v1/auth/register';
      const body = mode === 'login' ? { email, password } : { firstName, lastName, email, phone, password };
      const result = await apiRequest<{ accessToken: string }>(path, {
        method: 'POST', body: JSON.stringify(body),
      });
      saveToken(result.accessToken); window.location.assign('/dashboard');
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : `Could not ${mode === 'login' ? 'sign in' : 'create the account'}. Please retry.`);
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
          <p className="eyebrow">{mode === 'login' ? 'Welcome back' : 'Join Nexa Bank'}</p><h2 className="font-heading text-3xl font-semibold tracking-tight">{mode === 'login' ? 'Sign in to Nexa' : 'Create your profile'}</h2><p className="mt-2 text-sm text-muted-foreground">{mode === 'login' ? 'Use the customer credentials registered with your backend.' : 'Register securely, then open your first account.'}</p>
          <div className="mt-8 space-y-5">
            {mode === 'register' && <div className="grid grid-cols-2 gap-3"><div className="space-y-2"><Label htmlFor="firstName">First name</Label><Input id="firstName" autoComplete="given-name" value={firstName} onChange={(event) => setFirstName(event.target.value)} required maxLength={100} /></div><div className="space-y-2"><Label htmlFor="lastName">Last name</Label><Input id="lastName" autoComplete="family-name" value={lastName} onChange={(event) => setLastName(event.target.value)} required maxLength={100} /></div></div>}
            <div className="space-y-2"><Label htmlFor="email">Email address</Label><Input id="email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required /></div>
            {mode === 'register' && <div className="space-y-2"><Label htmlFor="phone">Phone number</Label><Input id="phone" type="tel" autoComplete="tel" placeholder="+919876543210" value={phone} onChange={(event) => setPhone(event.target.value)} required pattern="[+]?[0-9]{8,15}" /></div>}
            <div className="space-y-2"><Label htmlFor="password">Password</Label><Input id="password" type="password" autoComplete={mode === 'login' ? 'current-password' : 'new-password'} value={password} onChange={(event) => setPassword(event.target.value)} required minLength={12} maxLength={72} /><p className="text-xs text-muted-foreground">12–72 characters</p></div>
          </div>
          {error && <p role="alert" className="mt-4 rounded-lg bg-destructive/10 p-3 text-sm text-destructive">{error}</p>}
          <Button type="submit" size="lg" className="mt-6 w-full" disabled={pending}>{pending ? (mode === 'login' ? 'Signing in…' : 'Creating profile…') : (mode === 'login' ? 'Sign in' : 'Create profile')} {!pending && <ArrowRight data-icon="inline-end" />}</Button>
          <Button type="button" variant="ghost" className="mt-2 w-full" onClick={() => { setMode((current) => current === 'login' ? 'register' : 'login'); setError(''); }}>{mode === 'login' ? 'New to Nexa? Create a profile' : 'Already registered? Sign in'}</Button>
          <p className="mt-5 text-center text-xs leading-5 text-muted-foreground">The access token is kept only in this browser tab and cleared when the session ends.</p>
        </form>
      </section>
    </main>
  );
}
