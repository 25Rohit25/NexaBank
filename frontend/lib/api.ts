'use client';

export type Account = {
  accountId: string;
  customerId: string;
  accountNumber: string;
  type: 'SAVINGS' | 'CURRENT';
  balance: number;
  currency: string;
  status: string;
  createdAt: string;
};

export type Transaction = {
  transactionId: string;
  transferId: string | null;
  accountId: string;
  counterpartyAccountId: string | null;
  type: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER_IN' | 'TRANSFER_OUT' | 'PAYMENT';
  amount: number;
  currency: string;
  status: string;
  occurredAt: string;
  correlationId: string;
};

const API_BASE = process.env.NEXT_PUBLIC_NEXA_API_URL ?? 'http://localhost:8080';
const TOKEN_KEY = 'nexa_access_token';

export const demoAccounts: Account[] = [
  { accountId: 'ACC-DEMO-1001', customerId: 'CUSTOMER-DEMO', accountNumber: '100000001001', type: 'SAVINGS', balance: 92450, currency: 'INR', status: 'ACTIVE', createdAt: '2026-01-15T09:00:00Z' },
  { accountId: 'ACC-DEMO-2048', customerId: 'CUSTOMER-DEMO', accountNumber: '100000002048', type: 'CURRENT', balance: 32400, currency: 'INR', status: 'ACTIVE', createdAt: '2026-02-02T09:00:00Z' },
];

export const demoTransactions: Transaction[] = [
  { transactionId: 'TXN-DEMO-1', transferId: 'TRANSFER-DEMO-1', accountId: 'ACC-DEMO-1001', counterpartyAccountId: 'ACC-DEMO-2048', type: 'TRANSFER_OUT', amount: 2480, currency: 'INR', status: 'COMPLETED', occurredAt: '2026-09-01T09:30:00Z', correlationId: 'demo-1' },
  { transactionId: 'TXN-DEMO-2', transferId: null, accountId: 'ACC-DEMO-1001', counterpartyAccountId: null, type: 'DEPOSIT', amount: 86000, currency: 'INR', status: 'COMPLETED', occurredAt: '2026-08-28T08:00:00Z', correlationId: 'demo-2' },
  { transactionId: 'TXN-DEMO-3', transferId: 'TRANSFER-DEMO-2', accountId: 'ACC-DEMO-2048', counterpartyAccountId: 'ACC-DEMO-1001', type: 'TRANSFER_IN', amount: 3240, currency: 'INR', status: 'COMPLETED', occurredAt: '2026-08-27T12:10:00Z', correlationId: 'demo-3' },
];

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

export function getToken() {
  return typeof window === 'undefined' ? null : window.sessionStorage.getItem(TOKEN_KEY);
}

export function saveToken(token: string) {
  window.sessionStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  window.sessionStorage.removeItem(TOKEN_KEY);
}

export function customerIdFromToken(token: string) {
  try {
    const encoded = token.split('.')[1];
    const payload = encoded.replace(/-/g, '+').replace(/_/g, '/').padEnd(Math.ceil(encoded.length / 4) * 4, '=');
    return JSON.parse(window.atob(payload)).sub as string;
  } catch {
    return null;
  }
}

export async function apiRequest<T>(path: string, init: RequestInit = {}) {
  const token = getToken();
  const headers = new Headers(init.headers);
  if (!headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
  if (token) headers.set('Authorization', `Bearer ${token}`);
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers,
  });
  if (!response.ok) {
    const detail = await response.text();
    let message = `Request failed (${response.status})`;
    try {
      const problem = JSON.parse(detail) as { detail?: string; message?: string; title?: string };
      message = problem.detail || problem.message || problem.title || message;
    } catch {
      if (detail && detail.length < 240) message = detail;
    }
    throw new ApiError(response.status, message);
  }
  return response.json() as Promise<T>;
}

export function formatMoney(value: number, currency = 'INR') {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency }).format(value);
}
