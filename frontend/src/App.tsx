import { useEffect, useMemo, useState } from 'react';
import type { FormEvent, ReactNode } from 'react';
import { Activity, ArrowDownLeft, ArrowLeftRight, ArrowUpRight, Bot, CheckCircle2, ChevronDown, CircleHelp, CreditCard, LayoutDashboard, LoaderCircle, MessageCircle, Send, ShieldCheck, Sparkles, Wallet, X } from 'lucide-react';
import { askAssistant, getDashboard, getTransactions, transferMoney, type Dashboard, type Transaction } from './api';

type ChatMessage = { role: 'assistant' | 'user'; content: string; meta?: string };
const money = (value: number, currency = 'INR') => new Intl.NumberFormat('en-IN', { style: 'currency', currency, maximumFractionDigits: 2 }).format(value);
const dateText = (value: string) => new Date(value).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });

export default function App() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [accountId, setAccountId] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [active, setActive] = useState<'overview' | 'transactions'>('overview');
  const [showTransfer, setShowTransfer] = useState(false);
  const [recipient, setRecipient] = useState('INDB0001002');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('Personal transfer');
  const [transferBusy, setTransferBusy] = useState(false);
  const [transferMessage, setTransferMessage] = useState('');
  const [chatOpen, setChatOpen] = useState(false);
  const [chatInput, setChatInput] = useState('');
  const [chatBusy, setChatBusy] = useState(false);
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([{ role: 'assistant', content: 'Hi! I’m your IntelliBank AI assistant. Ask me about your balance, spending, transactions, or general banking documents.' }]);

  async function refresh(id = accountId) {
    setLoading(true); setError('');
    try { const [d, t] = await Promise.all([getDashboard(id), getTransactions(id)]); setDashboard(d); setTransactions(t); }
    catch (e) { setError(e instanceof Error ? e.message : 'Could not connect to the backend.'); }
    finally { setLoading(false); }
  }
  useEffect(() => { void refresh(accountId); }, [accountId]);
  const spendingByCategory = useMemo(() => {
    const sums = new Map<string, number>();
    transactions.filter(t => t.direction === 'DEBIT').forEach(t => sums.set(t.category, (sums.get(t.category) ?? 0) + t.amount));
    return [...sums.entries()].sort((a, b) => b[1] - a[1]).slice(0, 4);
  }, [transactions]);
  const totalCategorySpend = spendingByCategory.reduce((sum, item) => sum + item[1], 0) || 1;

  async function submitTransfer(e: FormEvent) {
    e.preventDefault(); setTransferMessage(''); setTransferBusy(true);
    try { const result = await transferMoney({ fromAccountId: accountId, toAccountNumber: recipient.trim(), amount: Number(amount), description: description.trim() }); setTransferMessage(result.message); setAmount(''); await refresh(accountId); }
    catch (e) { setTransferMessage(e instanceof Error ? e.message : 'Transfer failed.'); }
    finally { setTransferBusy(false); }
  }
  async function sendChat(text = chatInput) {
    const message = text.trim(); if (!message || chatBusy) return;
    setChatMessages(prev => [...prev, { role: 'user', content: message }]); setChatInput(''); setChatBusy(true);
    try { const result = await askAssistant(accountId, message); setChatMessages(prev => [...prev, { role: 'assistant', content: result.answer, meta: `${result.agent} · ${result.mode === 'ollama' ? 'Local LLM' : 'Demo fallback'}` }]); }
    catch (e) { setChatMessages(prev => [...prev, { role: 'assistant', content: e instanceof Error ? e.message : 'Assistant unavailable. Please try again.' }]); }
    finally { setChatBusy(false); }
  }

  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><div className="brand-mark"><Wallet size={22}/></div><div><strong>IntelliBank</strong><span>AI BANKING</span></div></div>
      <div className="side-label">WORKSPACE</div>
      <button className={`nav-item ${active === 'overview' ? 'selected' : ''}`} onClick={() => setActive('overview')}><LayoutDashboard size={18}/> Overview</button>
      <button className={`nav-item ${active === 'transactions' ? 'selected' : ''}`} onClick={() => setActive('transactions')}><ArrowLeftRight size={18}/> Transactions</button>
      <button className="nav-item" onClick={() => setShowTransfer(true)}><CreditCard size={18}/> Transfer money</button>
      <div className="sidebar-bottom"><div className="secure-icon"><ShieldCheck size={18}/></div><div><strong>Demo environment</strong><p>Uses synthetic data only.</p></div></div>
    </aside>
    <main className="main-content">
      <header className="topbar"><div className="breadcrumb">Workspace <span>/</span> <b>{active === 'overview' ? 'Overview' : 'Transactions'}</b></div><div className="top-actions"><span className="status-pill"><i/> System operational</span><div className="avatar">{dashboard?.customerName?.split(' ').map(n => n[0]).join('').slice(0, 2) ?? 'IB'}</div><div className="profile-text"><strong>{dashboard?.customerName ?? 'Demo customer'}</strong><span>Personal account</span></div><ChevronDown size={16}/></div></header>
      <section className="page-heading"><div><div className="eyebrow"><Sparkles size={14}/> YOUR MONEY, MADE CLEARER</div><h1>{active === 'overview' ? 'Good evening' : 'Transactions'}{active === 'overview' && dashboard ? `, ${dashboard.customerName.split(' ')[0]}` : ''}</h1><p>{active === 'overview' ? 'Here’s what’s happening with your finances today.' : 'Review the latest activity on your demo account.'}</p></div><div className="account-select"><label htmlFor="account">DEMO ACCOUNT</label><select id="account" value={accountId} onChange={e => setAccountId(Number(e.target.value))}><option value={1}>•• 1001 · Aarav Sharma</option><option value={2}>•• 1002 · Priya Sharma</option></select></div></section>
      {error && <div className="error-banner"><CircleHelp size={18}/><div><strong>Could not load your banking data</strong><p>{error}. Make sure the Spring Boot backend is running on port 8080.</p></div><button onClick={() => void refresh()} className="small-button">Retry</button></div>}
      {loading && !dashboard ? <div className="loading"><LoaderCircle className="spin"/> Connecting to IntelliBank API…</div> : dashboard && <>
        {active === 'overview' ? <>
          <div className="summary-grid">
            <div className="balance-card"><div className="balance-top"><span>Available balance</span><div className="balance-icon"><Wallet size={19}/></div></div><h2>{money(dashboard.balance, dashboard.currency)}</h2><div className="balance-foot"><span><i/> Demo account · {dashboard.accountNumber}</span><span className="balance-chip">Updated just now</span></div><div className="card-orb orb-one"/><div className="card-orb orb-two"/></div>
            <StatCard title="Total money in" value={money(dashboard.totalIncome, dashboard.currency)} caption="Recorded credits" icon={<ArrowDownLeft size={19}/>} tone="green"/>
            <StatCard title="Total money out" value={money(dashboard.totalExpenses, dashboard.currency)} caption="Recorded debits" icon={<ArrowUpRight size={19}/>} tone="orange"/>
          </div>
          <div className="content-grid"><section className="panel transactions-panel"><div className="panel-heading"><div><h3>Recent transactions</h3><p>Your latest account activity</p></div><button className="text-button" onClick={() => setActive('transactions')}>View all <span>→</span></button></div><TransactionTable transactions={dashboard.recentTransactions} currency={dashboard.currency}/></section>
            <section className="panel spending-panel"><div className="panel-heading"><div><h3>Spending snapshot</h3><p>By category · recent records</p></div><div className="round-icon"><Activity size={17}/></div></div>{spendingByCategory.length ? <div className="spending-list">{spendingByCategory.map(([category, value], i) => <div className="spending-item" key={category}><div className="spending-label"><span className={`category-dot dot-${i}`}/><span>{category}</span><strong>{money(value, dashboard.currency)}</strong></div><div className="progress-track"><div className={`progress-fill fill-${i}`} style={{ width: `${Math.max(4, value / totalCategorySpend * 100)}%` }}/></div></div>)}</div> : <div className="empty-state">No spending records yet.</div>}<div className="insight-box"><div className="insight-icon"><Sparkles size={17}/></div><div><strong>Want a clearer picture?</strong><p>Ask IntelliBank AI to explain your spending patterns.</p><button onClick={() => setChatOpen(true)}>Ask AI assistant <span>↗</span></button></div></div></section></div>
        </> : <section className="panel all-transactions"><div className="panel-heading"><div><h3>All transactions</h3><p>Latest 20 records from the backend</p></div><span className="record-count">{transactions.length} records</span></div><TransactionTable transactions={transactions} currency={dashboard.currency} showDate/></section>}
      </>}
      <footer className="footer"><span>© 2026 IntelliBank AI · Portfolio project</span><span><ShieldCheck size={14}/> Synthetic data · Not a real bank</span></footer>
    </main>
    <button className="chat-launcher" onClick={() => setChatOpen(true)} aria-label="Open AI assistant"><Bot size={21}/><span>Ask IntelliBank AI</span><span className="launcher-sparkle"><Sparkles size={14}/></span></button>
    {showTransfer && <div className="modal-backdrop" onMouseDown={e => { if (e.target === e.currentTarget) setShowTransfer(false); }}><section className="modal"><div className="modal-header"><div className="modal-title-icon"><ArrowLeftRight size={20}/></div><div><h2>Transfer money</h2><p>Move funds between demo accounts</p></div><button className="icon-button" onClick={() => setShowTransfer(false)} aria-label="Close"><X size={19}/></button></div><div className="demo-warning"><ShieldCheck size={17}/> Demo only. No real money moves.</div><form onSubmit={submitTransfer} className="transfer-form"><label>From account<input value={`${dashboard?.accountNumber ?? ''} · ${money(dashboard?.balance ?? 0)}`} readOnly/></label><label>Recipient account<select value={recipient} onChange={e => setRecipient(e.target.value)}><option value="INDB0001001">INDB0001001 · Aarav Sharma</option><option value="INDB0001002">INDB0001002 · Priya Sharma</option></select></label><label>Amount (INR)<input type="number" min="0.01" step="0.01" value={amount} onChange={e => setAmount(e.target.value)} placeholder="e.g. 500" required/></label><label>Description<input value={description} onChange={e => setDescription(e.target.value)} maxLength={100} required/></label>{transferMessage && <div className={transferMessage.toLowerCase().includes('completed') ? 'success-message' : 'error-message'}>{transferMessage}</div>}<button className="primary-button" disabled={transferBusy}>{transferBusy ? <LoaderCircle className="spin" size={17}/> : <ArrowLeftRight size={17}/>} {transferBusy ? 'Processing…' : 'Confirm demo transfer'}</button></form></section></div>}
    {chatOpen && <aside className="chat-panel"><div className="chat-header"><div className="ai-avatar"><Bot size={22}/><i/></div><div className="chat-header-text"><strong>IntelliBank AI</strong><span><i/> Banking assistant · Demo mode</span></div><button className="icon-button" onClick={() => setChatOpen(false)} aria-label="Close assistant"><X size={19}/></button></div><div className="chat-context"><ShieldCheck size={15}/> Answers use demo account data. Never share passwords or PINs.</div><div className="chat-messages">{chatMessages.map((m, i) => <div key={i} className={`chat-row ${m.role}`}><div className="message-avatar">{m.role === 'assistant' ? <Bot size={16}/> : 'You'}</div><div className="message-content"><div className="message-bubble">{m.content}</div>{m.meta && <span className="message-meta">{m.meta}</span>}</div></div>)}{chatBusy && <div className="chat-row assistant"><div className="message-avatar"><Bot size={16}/></div><div className="typing"><i/><i/><i/></div></div>}</div><div className="suggestion-row"><button onClick={() => void sendChat('How much have I spent?')}>Spending summary</button><button onClick={() => void sendChat('What is my balance?')}>My balance</button></div><form className="chat-compose" onSubmit={e => { e.preventDefault(); void sendChat(); }}><input value={chatInput} onChange={e => setChatInput(e.target.value)} placeholder="Ask about your banking…" aria-label="Message AI assistant"/><button disabled={!chatInput.trim() || chatBusy} aria-label="Send message"><Send size={18}/></button></form><div className="chat-disclaimer">AI responses may be inaccurate. Verify important information with your bank.</div></aside>}
  </div>;
}
function StatCard({ title, value, caption, icon, tone }: { title: string; value: string; caption: string; icon: ReactNode; tone: string }) { return <div className="stat-card"><div className="stat-top"><span>{title}</span><div className={`stat-icon ${tone}`}>{icon}</div></div><h2>{value}</h2><div className="stat-caption"><span className={`caption-dot ${tone}`}/>{caption}</div></div>; }
function TransactionTable({ transactions, currency, showDate = false }: { transactions: Transaction[]; currency: string; showDate?: boolean }) { if (!transactions.length) return <div className="empty-state">No transactions found for this account.</div>; return <div className="table-wrap"><table><thead><tr><th>TRANSACTION</th><th>CATEGORY</th>{showDate && <th>DATE</th>}<th>AMOUNT</th><th>STATUS</th></tr></thead><tbody>{transactions.map(t => <tr key={t.id}><td><div className="transaction-name"><div className={`transaction-icon ${t.direction === 'CREDIT' ? 'credit' : 'debit'}`}>{t.direction === 'CREDIT' ? <ArrowDownLeft size={17}/> : <ArrowUpRight size={17}/>}</div><div><strong>{t.description}</strong><span>{dateText(t.createdAt)}</span></div></div></td><td><span className="category-pill">{t.category}</span></td>{showDate && <td className="date-cell">{dateText(t.createdAt)}</td>}<td className={`amount-cell ${t.direction === 'CREDIT' ? 'positive' : ''}`}>{t.direction === 'CREDIT' ? '+' : '−'}{money(t.amount, currency)}</td><td><span className="complete-status"><CheckCircle2 size={13}/> Completed</span></td></tr>)}</tbody></table></div>; }
