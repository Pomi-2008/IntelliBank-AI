const API = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';
export type Transaction = { id: number; description: string; category: string; amount: number; direction: 'CREDIT' | 'DEBIT'; createdAt: string };
export type Dashboard = { accountId: number; customerName: string; accountNumber: string; balance: number; currency: string; totalIncome: number; totalExpenses: number; recentTransactions: Transaction[] };
async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API}${path}`, { ...options, headers: { 'Content-Type': 'application/json', ...options?.headers } });
  if (!response.ok) { let message = `Request failed (${response.status})`; try { const body = await response.json(); message = body.message ?? body.error ?? message; } catch {} throw new Error(message); }
  return response.json() as Promise<T>;
}
export const getDashboard = (id = 1) => request<Dashboard>(`/dashboard/${id}`);
export const getTransactions = (id = 1) => request<Transaction[]>(`/transactions/${id}`);
export const transferMoney = (payload: { fromAccountId: number; toAccountNumber: string; amount: number; description: string }) => request<{ message: string }>('/transfers', { method: 'POST', body: JSON.stringify(payload) });
export const askAssistant = (accountId: number, message: string) => request<{ answer: string; mode: string; agent: string; note?: string }>('/assistant', { method: 'POST', body: JSON.stringify({ accountId, message }) });
