import { NavLink, Outlet } from 'react-router-dom';

export function AppShell() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="app-header-inner">
          <NavLink to="/" className="app-brand">
            Support Ticket Management
          </NavLink>
          <nav className="app-nav" aria-label="Main navigation">
            <NavLink to="/tickets" className="nav-item">
              Tickets
            </NavLink>
            <NavLink to="/tickets/new" className="nav-item">
              Create Ticket
            </NavLink>
          </nav>
        </div>
      </header>
      <main className="app-main">
        <div className="page-container">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
