import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { transitionTicketStatus } from '../api/tickets';
import { ApiError } from '../types/api';
import type { Ticket, TicketStatus } from '../types/ticket';
import {
  STATUS_TRANSITION_NETWORK_ERROR_MESSAGE,
  TRANSITION_CONFLICT_MESSAGE,
  TRANSITION_ERROR_MESSAGE,
  TicketStatusActions,
} from './TicketStatusActions';

vi.mock('../api/tickets', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../api/tickets')>();
  return {
    ...actual,
    transitionTicketStatus: vi.fn(),
  };
});

const mockTransitionTicketStatus = vi.mocked(transitionTicketStatus);

const baseTicket: Ticket = {
  id: 42,
  title: 'Unable to login',
  description: 'User cannot login to the application.',
  priority: 'HIGH',
  status: 'OPEN',
  assignee: 'john.doe',
  createdAt: '2026-09-25T08:00:00Z',
  updatedAt: '2026-09-25T08:00:00Z',
};

const defaultProps = {
  ticketId: 42,
  currentStatus: 'OPEN' as TicketStatus,
  onTransitionSuccess: vi.fn(),
  onTicketNotFound: vi.fn(),
};

function renderStatusActions(overrides: Partial<typeof defaultProps> = {}) {
  const props = { ...defaultProps, ...overrides };
  return render(<TicketStatusActions {...props} />);
}

describe('TicketStatusActions', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal('confirm', vi.fn(() => true));
  });

  it('displays the current status', () => {
    renderStatusActions({ currentStatus: 'IN_PROGRESS' });

    expect(screen.getByText(/Current status:/i)).toBeInTheDocument();
    expect(screen.getByText('IN_PROGRESS')).toBeInTheDocument();
  });

  it('shows allowed actions for OPEN', () => {
    renderStatusActions({ currentStatus: 'OPEN' });

    expect(screen.getByRole('button', { name: 'Start Progress' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cancel Ticket' })).toBeInTheDocument();
  });

  it('shows allowed actions for IN_PROGRESS', () => {
    renderStatusActions({ currentStatus: 'IN_PROGRESS' });

    expect(screen.getByRole('button', { name: 'Resolve' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cancel Ticket' })).toBeInTheDocument();
  });

  it('shows Close action for RESOLVED', () => {
    renderStatusActions({ currentStatus: 'RESOLVED' });

    expect(screen.getByRole('button', { name: 'Close' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Cancel Ticket' })).not.toBeInTheDocument();
  });

  it('shows no transition actions for CLOSED', () => {
    renderStatusActions({ currentStatus: 'CLOSED' });

    expect(screen.getByText('No status actions available.')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Start Progress' })).not.toBeInTheDocument();
  });

  it('shows no transition actions for CANCELLED', () => {
    renderStatusActions({ currentStatus: 'CANCELLED' });

    expect(screen.getByText('No status actions available.')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Resolve' })).not.toBeInTheDocument();
  });

  it('sends a valid transition request with only the target status', async () => {
    mockTransitionTicketStatus.mockResolvedValue({
      ...baseTicket,
      status: 'IN_PROGRESS',
      updatedAt: '2026-09-25T09:00:00Z',
    });

    renderStatusActions();

    fireEvent.click(screen.getByRole('button', { name: 'Start Progress' }));

    await waitFor(() => {
      expect(mockTransitionTicketStatus).toHaveBeenCalledWith(42, { status: 'IN_PROGRESS' });
    });

    const payload = mockTransitionTicketStatus.mock.calls[0][1];
    expect(payload).not.toHaveProperty('id');
    expect(payload).not.toHaveProperty('title');
  });

  it('calls onTransitionSuccess after a successful transition', async () => {
    const onTransitionSuccess = vi.fn();
    const updatedTicket = {
      ...baseTicket,
      status: 'IN_PROGRESS' as TicketStatus,
      updatedAt: '2026-09-25T09:00:00Z',
    };
    mockTransitionTicketStatus.mockResolvedValue(updatedTicket);

    renderStatusActions({ onTransitionSuccess });

    fireEvent.click(screen.getByRole('button', { name: 'Start Progress' }));

    await waitFor(() => {
      expect(onTransitionSuccess).toHaveBeenCalledWith(updatedTicket);
    });
  });

  it('prevents duplicate transition requests while a request is in progress', async () => {
    let resolveTransition: (value: Ticket) => void = () => {};
    mockTransitionTicketStatus.mockImplementation(
      () =>
        new Promise<Ticket>((resolve) => {
          resolveTransition = resolve;
        }),
    );

    renderStatusActions();

    const startButton = screen.getByRole('button', { name: 'Start Progress' });
    fireEvent.click(startButton);
    fireEvent.click(startButton);

    expect(mockTransitionTicketStatus).toHaveBeenCalledTimes(1);

    resolveTransition({
      ...baseTicket,
      status: 'IN_PROGRESS',
      updatedAt: '2026-09-25T09:00:00Z',
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Start Progress' })).not.toBeDisabled();
    });
  });

  it('disables transition controls and shows loading state during request', async () => {
    let resolveTransition: (value: Ticket) => void = () => {};
    mockTransitionTicketStatus.mockImplementation(
      () =>
        new Promise<Ticket>((resolve) => {
          resolveTransition = resolve;
        }),
    );

    renderStatusActions();

    fireEvent.click(screen.getByRole('button', { name: 'Start Progress' }));

    expect(screen.getByText('Updating status...')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Start Progress' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Cancel Ticket' })).toBeDisabled();

    resolveTransition({
      ...baseTicket,
      status: 'IN_PROGRESS',
      updatedAt: '2026-09-25T09:00:00Z',
    });

    await waitFor(() => {
      expect(screen.queryByText('Updating status...')).not.toBeInTheDocument();
    });
  });

  it('handles backend rejection for invalid transitions', async () => {
    mockTransitionTicketStatus.mockRejectedValue(
      new ApiError(409, {
        status: 409,
        code: 'INVALID_STATUS_TRANSITION',
        message: TRANSITION_CONFLICT_MESSAGE,
      }),
    );

    renderStatusActions();

    fireEvent.click(screen.getByRole('button', { name: 'Start Progress' }));

    expect(await screen.findByText(TRANSITION_CONFLICT_MESSAGE)).toBeInTheDocument();
  });

  it('does not change displayed status when backend rejects transition', async () => {
    mockTransitionTicketStatus.mockRejectedValue(
      new ApiError(409, {
        status: 409,
        code: 'INVALID_STATUS_TRANSITION',
        message: TRANSITION_CONFLICT_MESSAGE,
      }),
    );

    renderStatusActions({ currentStatus: 'OPEN' });

    fireEvent.click(screen.getByRole('button', { name: 'Start Progress' }));

    await screen.findByText(TRANSITION_CONFLICT_MESSAGE);
    expect(screen.getByText('OPEN')).toBeInTheDocument();
    expect(defaultProps.onTransitionSuccess).not.toHaveBeenCalled();
  });

  it('calls onTicketNotFound for HTTP 404', async () => {
    const onTicketNotFound = vi.fn();
    mockTransitionTicketStatus.mockRejectedValue(
      new ApiError(404, {
        status: 404,
        code: 'TICKET_NOT_FOUND',
        message: 'Ticket with id 42 was not found.',
      }),
    );

    renderStatusActions({ onTicketNotFound });

    fireEvent.click(screen.getByRole('button', { name: 'Start Progress' }));

    await waitFor(() => {
      expect(onTicketNotFound).toHaveBeenCalledTimes(1);
    });
  });

  it('handles HTTP 409 conflict responses', async () => {
    mockTransitionTicketStatus.mockRejectedValue(
      new ApiError(409, {
        status: 409,
        code: 'INVALID_STATUS_TRANSITION',
        message: 'Transition from OPEN to RESOLVED is not allowed.',
      }),
    );

    renderStatusActions();

    fireEvent.click(screen.getByRole('button', { name: 'Start Progress' }));

    expect(
      await screen.findByText('Transition from OPEN to RESOLVED is not allowed.'),
    ).toBeInTheDocument();
  });

  it('handles HTTP 400 validation errors', async () => {
    mockTransitionTicketStatus.mockRejectedValue(
      new ApiError(400, {
        status: 400,
        code: 'VALIDATION_ERROR',
        message: 'Status must be one of OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED',
      }),
    );

    renderStatusActions();

    fireEvent.click(screen.getByRole('button', { name: 'Start Progress' }));

    expect(
      await screen.findByText(
        'Status must be one of OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED',
      ),
    ).toBeInTheDocument();
  });

  it('handles HTTP 500 errors', async () => {
    mockTransitionTicketStatus.mockRejectedValue(
      new ApiError(500, {
        status: 500,
        code: 'INTERNAL_SERVER_ERROR',
        message: 'An unexpected error occurred.',
      }),
    );

    renderStatusActions();

    fireEvent.click(screen.getByRole('button', { name: 'Start Progress' }));

    expect(await screen.findByText(TRANSITION_ERROR_MESSAGE)).toBeInTheDocument();
  });

  it('handles network failures', async () => {
    mockTransitionTicketStatus.mockRejectedValue(new Error('Network request failed'));

    renderStatusActions();

    fireEvent.click(screen.getByRole('button', { name: 'Start Progress' }));

    expect(await screen.findByText(STATUS_TRANSITION_NETWORK_ERROR_MESSAGE)).toBeInTheDocument();
  });

  it('requires confirmation before cancelling a ticket', async () => {
    const confirmMock = vi.fn(() => false);
    vi.stubGlobal('confirm', confirmMock);

    renderStatusActions({ currentStatus: 'OPEN' });

    fireEvent.click(screen.getByRole('button', { name: 'Cancel Ticket' }));

    expect(confirmMock).toHaveBeenCalledWith('Cancel this ticket?');
    expect(mockTransitionTicketStatus).not.toHaveBeenCalled();
  });
});
