import { AppShell } from '@/components/nexa/app-shell';
import { AssistantChat } from '@/components/nexa/assistant-chat';

export default function AssistantPage() {
  return <AppShell eyebrow="Verified help" title="Ask Nexa AI"><AssistantChat /></AppShell>;
}
