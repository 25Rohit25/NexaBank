import { AppShell } from '@/components/nexa/app-shell';
import { TransferForm } from '@/components/nexa/transfer-form';

export default function TransfersPage() {
  return <AppShell eyebrow="Move money" title="New transfer"><TransferForm /></AppShell>;
}
