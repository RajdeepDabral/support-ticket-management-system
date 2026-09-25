import { Link, Outlet } from 'react-router-dom';

export function AppShell() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <h1>Support Ticket Management</h1>
        <nav className="app-nav" aria-label="Main navigation">
          <Link to="/tickets" className="nav-item">Tickets</Link>
          <span className="nav-item nav-item--placeholder">Create Ticket</span>
        </nav>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  );
}
