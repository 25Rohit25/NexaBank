import { Bell, LockKeyhole, UserRound } from 'lucide-react';

import { AppShell } from '@/components/nexa/app-shell';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Switch } from '@/components/ui/switch';

const settings = [
  { title: 'Profile', detail: 'Rohit Singh · rohit@example.com', icon: UserRound },
  { title: 'Security', detail: 'JWT-protected session · customer role', icon: LockKeyhole },
];

export default function SettingsPage() {
  return <AppShell eyebrow="Preferences" title="Settings"><div className="grid gap-4 md:grid-cols-2">{settings.map(({ title, detail, icon: Icon }) => <Card key={title}><CardHeader><div className="mb-3 grid size-10 place-items-center rounded-xl bg-accent"><Icon className="size-5" /></div><CardTitle>{title}</CardTitle></CardHeader><CardContent className="text-sm text-muted-foreground">{detail}</CardContent></Card>)}<Card><CardHeader><div className="mb-3 grid size-10 place-items-center rounded-xl bg-accent"><Bell className="size-5" /></div><CardTitle>Notifications</CardTitle></CardHeader><CardContent className="flex items-center justify-between gap-4"><div><p className="text-sm font-medium">Transfer updates</p><p className="text-xs text-muted-foreground">Show successful transfer notifications</p></div><Switch defaultChecked aria-label="Transfer update notifications" /></CardContent></Card></div></AppShell>;
}
