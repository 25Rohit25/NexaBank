import { AppShell } from '@/components/nexa/app-shell';
import { TransactionsView } from '@/components/nexa/transactions-view';

export default function TransactionsPage() {
  return <AppShell eyebrow="Activity" title="Transactions"><TransactionsView /></AppShell>;
}
