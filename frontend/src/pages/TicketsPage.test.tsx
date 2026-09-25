import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { listTickets } from '../api/tickets';
import type { Ticket } from '../types/ticket';
import { TicketsPage } from './TicketsPage';

vi.mock('../api/tickets');

const mockListTickets = vi.mocked(listTickets);

const sampleTicket: Ticket = {
  id: 42,
  title: 'Login issue',
  description: 'User cannot login',
  priority: 'HIGH',
  status: 'OPEN',
  assignee: 'john.doe',
  createdAt: '2026-09-25T08:00:00Z',
  updatedAt: '2026-09-25T09:00:00Z',
};

function renderTicketsPage() {
  return render(
    <MemoryRouter initialEntries={['/tickets']}>
      <TicketsPage />
    </MemoryRouter>,
  );
}

describe('TicketsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the ticket list page and displays tickets from the API', async () => {
    mockListTickets.mockResolvedValue([sampleTicket]);

    renderTicketsPage();

    expect(screen.getByRole('heading', { name: 'Tickets' })).toBeInTheDocument();

    const row = await screen.findByRole('row', { name: /Login issue/i });

    expect(row).toHaveTextContent('42');
    expect(row).toHaveTextContent('Login issue');
    expect(row).toHaveTextContent('HIGH');
    expect(row).toHaveTextContent('OPEN');
    expect(row).toHaveTextContent('john.doe');
  });

  it('displays a loading state while tickets are being fetched', () => {
    mockListTickets.mockImplementation(() => new Promise(() => {}));

    renderTicketsPage();

    expect(screen.getByText('Loading tickets...')).toBeInTheDocument();
    expect(screen.queryByRole('table')).not.toBeInTheDocument();
  });

  it('displays an empty state when no tickets are returned', async () => {
    mockListTickets.mockResolvedValue([]);

    renderTicketsPage();

    await waitFor(() => {
      expect(screen.getByText('No tickets found yet.')).toBeInTheDocument();
    });
  });

  it('displays an error state with retry when the API request fails', async () => {
    mockListTickets
      .mockRejectedValueOnce(new Error('Network error'))
      .mockResolvedValueOnce([sampleTicket]);

    renderTicketsPage();

    await waitFor(() => {
      expect(screen.getByText('Unable to load tickets. Please try again.')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: 'Retry' }));

    await waitFor(() => {
      expect(screen.getByText('Login issue')).toBeInTheDocument();
    });

    expect(mockListTickets).toHaveBeenCalledTimes(2);
  });

  it('sends the keyword parameter when search is submitted', async () => {
    mockListTickets.mockResolvedValue([]);

    renderTicketsPage();

    await waitFor(() => {
      expect(mockListTickets).toHaveBeenCalledWith({});
    });

    fireEvent.change(screen.getByLabelText('Search tickets'), {
      target: { value: 'login' },
    });
    fireEvent.submit(screen.getByLabelText('Search tickets').closest('form')!);

    await waitFor(() => {
      expect(mockListTickets).toHaveBeenCalledWith({ keyword: 'login' });
    });
  });

  it('sends the status parameter when a status filter is selected', async () => {
    mockListTickets.mockResolvedValue([]);

    renderTicketsPage();

    await waitFor(() => {
      expect(mockListTickets).toHaveBeenCalledWith({});
    });

    fireEvent.change(screen.getByLabelText('Status'), {
      target: { value: 'OPEN' },
    });

    await waitFor(() => {
      expect(mockListTickets).toHaveBeenCalledWith({ status: 'OPEN' });
    });
  });

  it('sends both keyword and status parameters when search and filter are active', async () => {
    mockListTickets.mockResolvedValue([]);

    renderTicketsPage();

    await waitFor(() => {
      expect(mockListTickets).toHaveBeenCalledWith({});
    });

    fireEvent.change(screen.getByLabelText('Status'), {
      target: { value: 'OPEN' },
    });

    await waitFor(() => {
      expect(mockListTickets).toHaveBeenCalledWith({ status: 'OPEN' });
    });

    fireEvent.change(screen.getByLabelText('Search tickets'), {
      target: { value: 'login' },
    });
    fireEvent.submit(screen.getByLabelText('Search tickets').closest('form')!);

    await waitFor(() => {
      expect(mockListTickets).toHaveBeenCalledWith({
        keyword: 'login',
        status: 'OPEN',
      });
    });
  });

  it('shows a filtered empty state when search or status filters return no tickets', async () => {
    mockListTickets.mockResolvedValue([]);

    renderTicketsPage();

    await waitFor(() => {
      expect(mockListTickets).toHaveBeenCalledWith({});
    });

    fireEvent.change(screen.getByLabelText('Search tickets'), {
      target: { value: 'missing' },
    });
    fireEvent.submit(screen.getByLabelText('Search tickets').closest('form')!);

    await waitFor(() => {
      expect(mockListTickets).toHaveBeenCalledWith({ keyword: 'missing' });
    });

    await waitFor(() => {
      expect(screen.getByText('No tickets match your search criteria.')).toBeInTheDocument();
    });
  });

  it('links each ticket to the correct details route', async () => {
    mockListTickets.mockResolvedValue([sampleTicket]);

    renderTicketsPage();

    await waitFor(() => {
      expect(screen.getByRole('link', { name: 'View' })).toHaveAttribute('href', '/tickets/42');
    });
  });
});
