import { FormEvent } from 'react';
import { TICKET_STATUS_OPTIONS, type TicketStatusFilter } from '../types/ticket';

interface TicketFiltersProps {
  searchInput: string;
  selectedStatus: TicketStatusFilter;
  isLoading: boolean;
  onSearchInputChange: (value: string) => void;
  onSearchSubmit: () => void;
  onStatusChange: (status: TicketStatusFilter) => void;
}

export function TicketFilters({
  searchInput,
  selectedStatus,
  isLoading,
  onSearchInputChange,
  onSearchSubmit,
  onStatusChange,
}: TicketFiltersProps) {
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onSearchSubmit();
  };

  return (
    <div className="ticket-filters">
      <form className="ticket-search-form" onSubmit={handleSubmit}>
        <label htmlFor="ticket-search">Search tickets</label>
        <div className="ticket-search-controls">
          <input
            id="ticket-search"
            type="search"
            value={searchInput}
            placeholder="Search tickets..."
            onChange={(event) => onSearchInputChange(event.target.value)}
          />
          <button type="submit" disabled={isLoading}>
            Search
          </button>
        </div>
      </form>

      <div className="ticket-status-filter">
        <label htmlFor="ticket-status">Status</label>
        <select
          id="ticket-status"
          value={selectedStatus}
          onChange={(event) => onStatusChange(event.target.value as TicketStatusFilter)}
          disabled={isLoading}
        >
          <option value="ALL">All</option>
          {TICKET_STATUS_OPTIONS.map((status) => (
            <option key={status} value={status}>
              {status}
            </option>
          ))}
        </select>
      </div>
    </div>
  );
}
