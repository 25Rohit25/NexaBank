'use client';

export type Account = {
  accountId: string;
  accountType: 'SAVINGS' | 'CURRENT';
  accountNumber?: string;
  balance: number;
  currency: string;
  status: string;
};

export type Transaction = {
  transactionId: string;
  accountId: string;
  transactionType: string;
  amount: number;
  currency: string;
  description?: string;
  category?: string;
  status: string;
  createdAt: string;
};

const API_BASE = process.env.NEXT_PUBLIC_NEXA_API_URL ?? 'http://localhost:8080';
const TOKEN_KEY = 'nexa_access_token';

export const demoAccounts: Account[] = [
  { accountId: 'ACC-DEMO-1001', accountType: 'SAVINGS', balance: 92450, currency: 'INR', status: 'ACTIVE' },
  { accountId: 'ACC-DEMO-2048', accountType: 'CURRENT', balance: 32400, currency: 'INR', status: 'ACTIVE' },
];

export const demoTransactions: Transaction[] = [
  { transactionId: 'TXN-DEMO-1', accountId: 'ACC-DEMO-1001', transactionType: 'PAYMENT', amount: 2480, currency: 'INR', description: 'Riverside Market', category: 'GROCERIES', status: 'COMPLETED', createdAt: '2026-09-01T09:30:00Z' },
  { transactionId: 'TXN-DEMO-2', accountId: 'ACC-DEMO-1001', transactionType: 'DEPOSIT', amount: 86000, currency: 'INR', description: 'Salary credit', category: 'INCOME', status: 'COMPLETED', createdAt: '2026-08-28T08:00:00Z' },
  { transactionId: 'TXN-DEMO-3', accountId: 'ACC-DEMO-2048', transactionType: 'PAYMENT', amount: 3240, currency: 'INR', description: 'Metro utilities', category: 'BILLS', status: 'COMPLETED', createdAt: '2026-08-27T12:10:00Z' },
];

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
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(window.atob(payload)).sub as string;
  } catch {
    return null;
  }
}

export async function apiRequest<T>(path: string, init: RequestInit = {}) {
  const token = getToken();
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers,
    },
  });
  if (!response.ok) {
    const detail = await response.text();
    throw new Error(detail || `Request failed (${response.status})`);
  }
  return response.json() as Promise<T>;
}

export function formatMoney(value: number, currency = 'INR') {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency }).format(value);
}
