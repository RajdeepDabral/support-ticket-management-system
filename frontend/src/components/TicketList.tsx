import { Link } from 'react-router-dom';
import type { Ticket } from '../types/ticket';
import { TicketRow } from './TicketRow';

interface TicketListProps {
  tickets: Ticket[];
  isLoading: boolean;
  error: string | null;
  hasActiveFilters: boolean;
  onRetry: () => void;
}

export function TicketList({
  tickets,
  isLoading,
  error,
  hasActiveFilters,
  onRetry,
}: TicketListProps) {
  if (isLoading) {
    return (
      <div className="loading-state" role="status" aria-live="polite">
        <span className="loading-spinner" aria-hidden="true" />
        <span>Loading tickets...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="ticket-list-error" role="alert">
        <p>{error}</p>
        <button type="button" onClick={onRetry}>
          Retry
        </button>
      </div>
    );
  }

  if (tickets.length === 0) {
    return (
      <div className="empty-state" role="status">
        <p>
          {hasActiveFilters
            ? 'No tickets match your search criteria.'
            : 'No tickets found yet.'}
        </p>
        {!hasActiveFilters && (
          <Link to="/tickets/new" className="button-primary">
            Create your first ticket
          </Link>
        )}
      </div>
    );
  }

  return (
    <div className="ticket-table-wrapper card">
      <table className="ticket-table">
        <thead>
          <tr>
            <th scope="col">ID</th>
            <th scope="col">Title</th>
            <th scope="col">Priority</th>
            <th scope="col">Status</th>
            <th scope="col">Assignee</th>
            <th scope="col">Updated</th>
            <th scope="col">Actions</th>
          </tr>
        </thead>
        <tbody>
          {tickets.map((ticket) => (
            <TicketRow key={ticket.id} ticket={ticket} />
          ))}
        </tbody>
      </table>
    </div>
  );
}
