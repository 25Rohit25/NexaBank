'use client';

import { useState } from 'react';
import { Bot, Send, Sparkles, User } from 'lucide-react';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { apiRequest } from '@/lib/api';

type Message = { role: 'user' | 'assistant'; content: string };

export function AssistantChat() {
  const [messages, setMessages] = useState<Message[]>([{ role: 'assistant', content: 'Hello, Rohit. I can check live account information, prepare transfers, or explain Nexa Bank policies. What would you like to know?' }]);
  const [input, setInput] = useState(''); const [pending, setPending] = useState(false);

  async function send(message = input) {
    const clean = message.trim(); if (!clean || pending) return;
    setMessages((current) => [...current, { role: 'user', content: clean }]); setInput(''); setPending(true);
    try {
      const result = await apiRequest<{ message: string }>('/api/v1/agent/chat', { method: 'POST', body: JSON.stringify({ message: clean }) });
      setMessages((current) => [...current, { role: 'assistant', content: result.message }]);
    } catch {
      setMessages((current) => [...current, { role: 'assistant', content: "I can't verify banking information right now. Sign in and start the agent, MCP, and banking services before retrying." }]);
    } finally { setPending(false); }
  }

  return <div className="grid min-h-[650px] overflow-hidden rounded-2xl border bg-card lg:grid-cols-[260px_1fr]">
    <aside className="border-b bg-accent/40 p-5 lg:border-b-0 lg:border-r"><div className="flex items-center gap-2"><div className="grid size-9 place-items-center rounded-xl bg-primary text-primary-foreground"><Sparkles className="size-4" /></div><div><p className="font-heading font-semibold">Nexa AI</p><Badge variant="secondary">Grounded</Badge></div></div><p className="mt-5 text-sm leading-6 text-muted-foreground">Live balances and transactions come from authenticated tools. Fees and rules come from retrieved bank policies.</p><div className="mt-6 space-y-2">{['What is my balance?', 'Show transactions above ₹5,000', 'What are international transfer fees?'].map((prompt) => <Button key={prompt} variant="outline" className="h-auto w-full justify-start bg-card py-2.5 text-left text-xs whitespace-normal" onClick={() => void send(prompt)}>{prompt}</Button>)}</div></aside>
    <section className="flex min-h-0 flex-col"><div className="flex-1 space-y-5 overflow-y-auto p-5 sm:p-7">{messages.map((message, index) => <div key={`${message.role}-${index}`} className={`flex gap-3 ${message.role === 'user' ? 'justify-end' : ''}`}>{message.role === 'assistant' && <div className="grid size-8 shrink-0 place-items-center rounded-lg bg-primary text-primary-foreground"><Bot className="size-4" /></div>}<div className={`max-w-[78%] rounded-2xl px-4 py-3 text-sm leading-6 ${message.role === 'user' ? 'bg-primary text-primary-foreground' : 'bg-muted'}`}>{message.content}</div>{message.role === 'user' && <div className="grid size-8 shrink-0 place-items-center rounded-lg bg-secondary"><User className="size-4" /></div>}</div>)}{pending && <div className="text-sm text-muted-foreground">Nexa is checking verified sources…</div>}</div><form className="border-t p-4" onSubmit={(event) => { event.preventDefault(); void send(); }}><div className="flex items-end gap-2"><Textarea value={input} onChange={(event) => setInput(event.target.value)} placeholder="Type your banking request…" aria-label="Message Nexa AI" className="min-h-11 resize-none" maxLength={2000} /><Button type="submit" size="icon-lg" aria-label="Send message" disabled={pending || !input.trim()}><Send /></Button></div><p className="mt-2 text-[11px] text-muted-foreground">Transfers require explicit confirmation. Never share passwords or tokens.</p></form></section>
  </div>;
}
