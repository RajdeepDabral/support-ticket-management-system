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
    return <p role="status">Loading tickets...</p>;
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
      <p role="status">
        {hasActiveFilters ? 'No tickets match your search criteria.' : 'No tickets found.'}
      </p>
    );
  }

  return (
    <div className="ticket-table-wrapper">
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
