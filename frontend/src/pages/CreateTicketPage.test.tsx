import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createTicket } from '../api/tickets';
import { ApiError } from '../types/api';
import type { Ticket } from '../types/ticket';
import { CreateTicketPage } from './CreateTicketPage';

const mockNavigate = vi.fn();

vi.mock('../api/tickets');
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const mockCreateTicket = vi.mocked(createTicket);

const createdTicket: Ticket = {
  id: 42,
  title: 'Unable to login',
  description: 'User cannot login to the application.',
  priority: 'HIGH',
  status: 'OPEN',
  assignee: 'john.doe',
  createdAt: '2026-09-25T08:00:00Z',
  updatedAt: '2026-09-25T08:00:00Z',
};

function renderCreateTicketPage() {
  return render(
    <MemoryRouter initialEntries={['/tickets/new']}>
      <Routes>
        <Route path="/tickets/new" element={<CreateTicketPage />} />
        <Route path="/tickets" element={<h2>Tickets</h2>} />
      </Routes>
    </MemoryRouter>,
  );
}

function fillValidForm() {
  fireEvent.change(screen.getByLabelText(/Title/i), {
    target: { value: 'Unable to login' },
  });
  fireEvent.change(screen.getByLabelText(/Description/i), {
    target: { value: 'User cannot login to the application.' },
  });
  fireEvent.change(screen.getByLabelText(/Priority/i), {
    target: { value: 'HIGH' },
  });
  fireEvent.change(screen.getByLabelText(/Assignee/i), {
    target: { value: 'john.doe' },
  });
}

describe('CreateTicketPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the create ticket form with required fields', () => {
    renderCreateTicketPage();

    expect(screen.getByRole('heading', { name: 'Create Ticket' })).toBeInTheDocument();
    expect(screen.getByLabelText(/Title/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Description/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Priority/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Assignee/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Create Ticket' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Cancel' })).toHaveAttribute('href', '/tickets');
  });

  it('rejects a blank title before submitting', async () => {
    renderCreateTicketPage();

    fillValidForm();
    fireEvent.change(screen.getByLabelText(/Title/i), { target: { value: '   ' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create Ticket' }));

    expect(await screen.findByText('Title is required.')).toBeInTheDocument();
    expect(mockCreateTicket).not.toHaveBeenCalled();
  });

  it('rejects a blank description before submitting', async () => {
    renderCreateTicketPage();

    fillValidForm();
    fireEvent.change(screen.getByLabelText(/Description/i), { target: { value: '   ' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create Ticket' }));

    expect(await screen.findByText('Description is required.')).toBeInTheDocument();
    expect(mockCreateTicket).not.toHaveBeenCalled();
  });

  it('rejects a missing priority before submitting', async () => {
    renderCreateTicketPage();

    fillValidForm();
    fireEvent.change(screen.getByLabelText(/Priority/i), { target: { value: '' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create Ticket' }));

    expect(await screen.findByText('Priority is required.')).toBeInTheDocument();
    expect(mockCreateTicket).not.toHaveBeenCalled();
  });

  it('rejects a blank assignee before submitting', async () => {
    renderCreateTicketPage();

    fillValidForm();
    fireEvent.change(screen.getByLabelText(/Assignee/i), { target: { value: '   ' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create Ticket' }));

    expect(await screen.findByText('Assignee is required.')).toBeInTheDocument();
    expect(mockCreateTicket).not.toHaveBeenCalled();
  });

  it('submits a valid form with only allowed fields', async () => {
    mockCreateTicket.mockResolvedValue(createdTicket);

    renderCreateTicketPage();
    fillValidForm();
    fireEvent.click(screen.getByRole('button', { name: 'Create Ticket' }));

    await waitFor(() => {
      expect(mockCreateTicket).toHaveBeenCalledWith({
        title: 'Unable to login',
        description: 'User cannot login to the application.',
        priority: 'HIGH',
        assignee: 'john.doe',
      });
    });

    const payload = mockCreateTicket.mock.calls[0][0];
    expect(payload).not.toHaveProperty('id');
    expect(payload).not.toHaveProperty('status');
    expect(payload).not.toHaveProperty('createdAt');
    expect(payload).not.toHaveProperty('updatedAt');
  });

  it('disables submit and prevents duplicate requests while creating', async () => {
    let resolveCreate: (value: Ticket) => void = () => {};
    mockCreateTicket.mockImplementation(
      () =>
        new Promise<Ticket>((resolve) => {
          resolveCreate = resolve;
        }),
    );

    renderCreateTicketPage();
    fillValidForm();

    fireEvent.click(screen.getByRole('button', { name: 'Create Ticket' }));
    expect(screen.getByRole('button', { name: 'Creating...' })).toBeDisabled();

    fireEvent.click(screen.getByRole('button', { name: 'Creating...' }));
    expect(mockCreateTicket).toHaveBeenCalledTimes(1);

    resolveCreate(createdTicket);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/tickets/42');
    });
  });

  it('navigates to the created ticket on success', async () => {
    mockCreateTicket.mockResolvedValue(createdTicket);

    renderCreateTicketPage();
    fillValidForm();
    fireEvent.click(screen.getByRole('button', { name: 'Create Ticket' }));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/tickets/42');
    });
  });

  it('displays backend field validation errors for HTTP 400', async () => {
    mockCreateTicket.mockRejectedValue(
      new ApiError(400, {
        status: 400,
        code: 'VALIDATION_ERROR',
        message: 'Request validation failed.',
        fieldErrors: {
          title: 'Title must not be blank',
        },
      }),
    );

    renderCreateTicketPage();
    fillValidForm();
    fireEvent.click(screen.getByRole('button', { name: 'Create Ticket' }));

    expect(await screen.findByText('Title must not be blank')).toBeInTheDocument();
  });

  it('displays a meaningful message for HTTP 500 errors', async () => {
    mockCreateTicket.mockRejectedValue(
      new ApiError(500, {
        status: 500,
        code: 'INTERNAL_SERVER_ERROR',
        message: 'An unexpected error occurred.',
      }),
    );

    renderCreateTicketPage();
    fillValidForm();
    fireEvent.click(screen.getByRole('button', { name: 'Create Ticket' }));

    expect(
      await screen.findByText('Unable to create ticket. Please try again.'),
    ).toBeInTheDocument();
  });

  it('displays a meaningful message for network failures and allows retry', async () => {
    mockCreateTicket
      .mockRejectedValueOnce(new TypeError('Failed to fetch'))
      .mockResolvedValueOnce(createdTicket);

    renderCreateTicketPage();
    fillValidForm();
    fireEvent.click(screen.getByRole('button', { name: 'Create Ticket' }));

    expect(
      await screen.findByText(
        'Unable to connect to the server. Please check your connection and try again.',
      ),
    ).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Create Ticket' }));

    await waitFor(() => {
      expect(mockCreateTicket).toHaveBeenCalledTimes(2);
      expect(mockNavigate).toHaveBeenCalledWith('/tickets/42');
    });
  });

  it('returns to the ticket list when cancel is selected', () => {
    renderCreateTicketPage();

    fireEvent.click(screen.getByRole('link', { name: 'Cancel' }));

    expect(screen.getByRole('heading', { name: 'Tickets' })).toBeInTheDocument();
  });
});
