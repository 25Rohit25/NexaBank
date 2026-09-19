'use client';

import { useEffect, useState } from 'react';
import { Bell, LoaderCircle, LockKeyhole, Save, UserRound } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { apiRequest, Customer, customerIdFromToken, getToken } from '@/lib/api';

const NOTIFICATION_KEY = 'nexa_transfer_notifications';

export function SettingsView() {
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [firstName, setFirstName] = useState(''); const [lastName, setLastName] = useState(''); const [phone, setPhone] = useState('');
  const [notifications, setNotifications] = useState(true);
  const [loading, setLoading] = useState(true); const [saving, setSaving] = useState(false); const [message, setMessage] = useState('');

  useEffect(() => {
    const token = getToken(); const customerId = token ? customerIdFromToken(token) : null;
    const storedPreference = window.localStorage.getItem(NOTIFICATION_KEY);
    if (storedPreference !== null) void Promise.resolve().then(() => setNotifications(storedPreference === 'true'));
    if (!customerId) { void Promise.resolve().then(() => { setMessage('Sign in to manage your profile.'); setLoading(false); }); return; }
    apiRequest<Customer>(`/api/v1/customers/${customerId}`)
      .then((result) => { setCustomer(result); setFirstName(result.firstName); setLastName(result.lastName); setPhone(result.phone); })
      .catch((reason: Error) => setMessage(reason.message))
      .finally(() => setLoading(false));
  }, []);

  async function saveProfile() {
    if (!customer) return;
    setSaving(true); setMessage('');
    try {
      const updated = await apiRequest<Customer>(`/api/v1/customers/${customer.id}`, { method: 'PUT', body: JSON.stringify({ firstName, lastName, phone }) });
      setCustomer(updated); setMessage('Profile saved.');
    } catch (reason) { setMessage(reason instanceof Error ? reason.message : 'Profile could not be saved.'); }
    finally { setSaving(false); }
  }

  function updateNotifications(value: boolean) {
    setNotifications(value); window.localStorage.setItem(NOTIFICATION_KEY, String(value));
  }

  if (loading) return <div className="flex items-center gap-2 rounded-xl border bg-card p-6 text-sm text-muted-foreground"><LoaderCircle className="size-4 animate-spin" /> Loading your settings…</div>;

  return <div className="grid gap-4 md:grid-cols-2">
    <Card><CardHeader><div className="mb-3 grid size-10 place-items-center rounded-xl bg-accent"><UserRound className="size-5" /></div><CardTitle>Profile</CardTitle></CardHeader><CardContent>
      {customer ? <form className="space-y-4" onSubmit={(event) => { event.preventDefault(); void saveProfile(); }}><div className="grid grid-cols-2 gap-3"><div className="space-y-2"><Label htmlFor="settings-first-name">First name</Label><Input id="settings-first-name" value={firstName} onChange={(event) => setFirstName(event.target.value)} required maxLength={100} /></div><div className="space-y-2"><Label htmlFor="settings-last-name">Last name</Label><Input id="settings-last-name" value={lastName} onChange={(event) => setLastName(event.target.value)} required maxLength={100} /></div></div><div className="space-y-2"><Label htmlFor="settings-phone">Phone</Label><Input id="settings-phone" type="tel" value={phone} onChange={(event) => setPhone(event.target.value)} required pattern="[+]?[0-9]{8,15}" /></div><p className="truncate text-xs text-muted-foreground">{customer.email}</p><Button type="submit" disabled={saving}>{saving ? <LoaderCircle className="animate-spin" /> : <Save />}{saving ? 'Saving…' : 'Save profile'}</Button></form> : <p className="text-sm text-muted-foreground">Profile information is available after sign-in.</p>}
      {message && <output className="mt-4 block rounded-lg bg-muted p-3 text-sm" aria-live="polite">{message}</output>}
    </CardContent></Card>
    <div className="space-y-4"><Card><CardHeader><div className="mb-3 grid size-10 place-items-center rounded-xl bg-accent"><LockKeyhole className="size-5" /></div><CardTitle>Security</CardTitle></CardHeader><CardContent className="text-sm text-muted-foreground">{customer ? `JWT-protected session · ${customer.status.toLowerCase()} customer` : 'No active customer session'}</CardContent></Card><Card><CardHeader><div className="mb-3 grid size-10 place-items-center rounded-xl bg-accent"><Bell className="size-5" /></div><CardTitle>Notifications</CardTitle></CardHeader><CardContent className="flex items-center justify-between gap-4"><div><p className="text-sm font-medium">Transfer updates</p><p className="text-xs text-muted-foreground">Remember this display preference on this device</p></div><Switch checked={notifications} onCheckedChange={updateNotifications} aria-label="Transfer update notifications" /></CardContent></Card></div>
  </div>;
}
