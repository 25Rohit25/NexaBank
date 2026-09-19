import { AppShell } from '@/components/nexa/app-shell';
import { SettingsView } from '@/components/nexa/settings-view';

export default function SettingsPage() {
  return <AppShell eyebrow="Preferences" title="Settings"><SettingsView /></AppShell>;
}
