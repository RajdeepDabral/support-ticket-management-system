import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listTickets } from '../api/tickets';
import { TicketFilters } from '../components/TicketFilters';
import { TicketList } from '../components/TicketList';
import type { ListTicketsParams, Ticket, TicketStatusFilter } from '../types/ticket';

const LOAD_ERROR_MESSAGE = 'Unable to load tickets. Please try again.';

function buildListParams(
  appliedKeyword: string,
  selectedStatus: TicketStatusFilter,
): ListTicketsParams {
  const params: ListTicketsParams = {};

  const trimmedKeyword = appliedKeyword.trim();
  if (trimmedKeyword) {
    params.keyword = trimmedKeyword;
  }

  if (selectedStatus !== 'ALL') {
    params.status = selectedStatus;
  }

  return params;
}

export function TicketsPage() {
  const [searchInput, setSearchInput] = useState('');
  const [appliedKeyword, setAppliedKeyword] = useState('');
  const [selectedStatus, setSelectedStatus] = useState<TicketStatusFilter>('ALL');
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadTickets = useCallback(async (params: ListTicketsParams) => {
    setIsLoading(true);
    setError(null);

    try {
      const data = await listTickets(params);
      setTickets(data);
    } catch {
      setTickets([]);
      setError(LOAD_ERROR_MESSAGE);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const currentParams = buildListParams(appliedKeyword, selectedStatus);

  useEffect(() => {
    void loadTickets(currentParams);
  }, [loadTickets, appliedKeyword, selectedStatus]);

  const handleSearchSubmit = () => {
    setAppliedKeyword(searchInput.trim());
  };

  const handleStatusChange = (status: TicketStatusFilter) => {
    setSelectedStatus(status);
  };

  const handleRetry = () => {
    void loadTickets(currentParams);
  };

  const hasActiveFilters = appliedKeyword.trim().length > 0 || selectedStatus !== 'ALL';

  return (
    <section className="tickets-page">
      <div className="tickets-page-header">
        <h2>Tickets</h2>
        <Link to="/tickets/new" className="button-link">Create Ticket</Link>
      </div>

      <TicketFilters
        searchInput={searchInput}
        selectedStatus={selectedStatus}
        isLoading={isLoading}
        onSearchInputChange={setSearchInput}
        onSearchSubmit={handleSearchSubmit}
        onStatusChange={handleStatusChange}
      />

      <TicketList
        tickets={tickets}
        isLoading={isLoading}
        error={error}
        hasActiveFilters={hasActiveFilters}
        onRetry={handleRetry}
      />
    </section>
  );
}
