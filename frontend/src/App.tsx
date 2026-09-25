import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './components/AppShell';
import { ErrorBoundary } from './components/ErrorBoundary';
import { HomePage } from './pages/HomePage';
import { CreateTicketPage } from './pages/CreateTicketPage';
import { TicketDetailsPage } from './pages/TicketDetailsPage';
import { TicketsPage } from './pages/TicketsPage';

export function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<AppShell />}>
            <Route index element={<HomePage />} />
            <Route path="tickets/new" element={<CreateTicketPage />} />
            <Route path="tickets" element={<TicketsPage />} />
            <Route path="tickets/:ticketId" element={<TicketDetailsPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </ErrorBoundary>
  );
}
