import { AppShell } from '@/components/nexa/app-shell';
import { AccountsView } from '@/components/nexa/accounts-view';

export default function AccountsPage() {
  return <AppShell eyebrow="Your money" title="Accounts"><AccountsView /></AppShell>;
}
