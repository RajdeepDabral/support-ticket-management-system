import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { addComment, getTicket, transitionTicketStatus, updateTicket } from '../api/tickets';
import { ApiError } from '../types/api';
import type { Ticket, TicketDetail } from '../types/ticket';
import { TicketDetailsPage } from './TicketDetailsPage';

vi.mock('../api/tickets', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../api/tickets')>();
  return {
    ...actual,
    getTicket: vi.fn(),
    updateTicket: vi.fn(),
    addComment: vi.fn(),
    transitionTicketStatus: vi.fn(),
  };
});

const mockGetTicket = vi.mocked(getTicket);
const mockUpdateTicket = vi.mocked(updateTicket);
const mockAddComment = vi.mocked(addComment);
const mockTransitionTicketStatus = vi.mocked(transitionTicketStatus);

const baseTicket: TicketDetail = {
  id: 42,
  title: 'Unable to login',
  description: 'User cannot login to the application.',
  priority: 'HIGH',
  status: 'OPEN',
  assignee: 'john.doe',
  createdAt: '2026-09-25T08:00:00Z',
  updatedAt: '2026-09-25T08:00:00Z',
  comments: [],
};

function renderTicketDetailsPage(ticketId = '42') {
  return render(
    <MemoryRouter initialEntries={[`/tickets/${ticketId}`]}>
      <Routes>
        <Route path="/tickets" element={<h2>Tickets</h2>} />
        <Route path="/tickets/:ticketId" element={<TicketDetailsPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

async function waitForTicketToLoad() {
  await waitFor(() => {
    expect(screen.getByText('Unable to login')).toBeInTheDocument();
  });
}

describe('TicketDetailsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGetTicket.mockResolvedValue(baseTicket);
    vi.stubGlobal('confirm', vi.fn(() => true));
  });

  it('loads and displays ticket details', async () => {
    renderTicketDetailsPage();

    await waitForTicketToLoad();

    expect(mockGetTicket).toHaveBeenCalledWith(42);
    expect(screen.getByText('42')).toBeInTheDocument();
    expect(screen.getByText('Unable to login')).toBeInTheDocument();
    expect(screen.getByText('User cannot login to the application.')).toBeInTheDocument();
    expect(screen.getByText('HIGH')).toBeInTheDocument();
    expect(screen.getAllByText('OPEN').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('john.doe')).toBeInTheDocument();
  });

  it('displays a loading state while fetching the ticket', () => {
    mockGetTicket.mockImplementation(
      () =>
        new Promise<TicketDetail>(() => {
          /* pending */
        }),
    );

    renderTicketDetailsPage();

    expect(screen.getByText('Loading ticket...')).toBeInTheDocument();
    expect(screen.getByText('Loading comments...')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Edit Ticket' })).not.toBeInTheDocument();
    expect(screen.queryByText('No comments yet.')).not.toBeInTheDocument();
  });

  it('displays a not-found state for HTTP 404', async () => {
    mockGetTicket.mockRejectedValue(
      new ApiError(404, {
        status: 404,
        code: 'TICKET_NOT_FOUND',
        message: 'Ticket with id 42 was not found.',
      }),
    );

    renderTicketDetailsPage();

    expect(await screen.findByText('Ticket not found.')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Return to ticket list' })).toHaveAttribute(
      'href',
      '/tickets',
    );
  });

  it('displays a general load error and allows retry', async () => {
    mockGetTicket
      .mockRejectedValueOnce(
        new ApiError(500, {
          status: 500,
          code: 'INTERNAL_SERVER_ERROR',
          message: 'An unexpected error occurred.',
        }),
      )
      .mockResolvedValueOnce(baseTicket);

    renderTicketDetailsPage();

    expect(await screen.findByText('Unable to load ticket. Please try again.')).toBeInTheDocument();

    fireEvent.click(screen.getAllByRole('button', { name: 'Try again' })[0]);

    await waitForTicketToLoad();
    expect(mockGetTicket).toHaveBeenCalledTimes(2);
  });

  it('opens edit mode when Edit Ticket is selected', async () => {
    renderTicketDetailsPage();
    await waitForTicketToLoad();

    fireEvent.click(screen.getByRole('button', { name: 'Edit Ticket' }));

    expect(screen.getByLabelText(/Title/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Description/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Priority/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Assignee/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Save' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument();
  });

  it('allows editable fields to be changed in edit mode', async () => {
    renderTicketDetailsPage();
    await waitForTicketToLoad();

    fireEvent.click(screen.getByRole('button', { name: 'Edit Ticket' }));

    fireEvent.change(screen.getByLabelText(/Title/i), { target: { value: 'Updated title' } });
    fireEvent.change(screen.getByLabelText(/Description/i), {
      target: { value: 'Updated description.' },
    });
    fireEvent.change(screen.getByLabelText(/Priority/i), { target: { value: 'CRITICAL' } });
    fireEvent.change(screen.getByLabelText(/Assignee/i), { target: { value: 'jane.doe' } });

    expect(screen.getByLabelText(/Title/i)).toHaveValue('Updated title');
    expect(screen.getByLabelText(/Description/i)).toHaveValue('Updated description.');
    expect(screen.getByLabelText(/Priority/i)).toHaveValue('CRITICAL');
    expect(screen.getByLabelText(/Assignee/i)).toHaveValue('jane.doe');
  });

  it('keeps status read-only in edit mode', async () => {
    renderTicketDetailsPage();
    await waitForTicketToLoad();

    fireEvent.click(screen.getByRole('button', { name: 'Edit Ticket' }));

    expect(screen.queryByLabelText(/^Status$/i)).not.toBeInTheDocument();
    expect(screen.queryByRole('combobox', { name: /^Status$/i })).not.toBeInTheDocument();

    const statusLabel = screen.getByText('Status');
    const statusItem = statusLabel.closest('.ticket-details-meta-item');
    expect(statusItem).not.toBeNull();
    expect(within(statusItem as HTMLElement).getByText('OPEN')).toBeInTheDocument();
  });

  it('validates required fields before saving', async () => {
    renderTicketDetailsPage();
    await waitForTicketToLoad();

    fireEvent.click(screen.getByRole('button', { name: 'Edit Ticket' }));
    fireEvent.change(screen.getByLabelText(/Title/i), { target: { value: '   ' } });
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('Title is required.')).toBeInTheDocument();
    expect(mockUpdateTicket).not.toHaveBeenCalled();
  });

  it('sends the correct update request without status', async () => {
    const updatedTicket: Ticket = {
      ...baseTicket,
      title: 'Updated title',
      description: 'Updated description.',
      priority: 'CRITICAL',
      assignee: 'jane.doe',
      updatedAt: '2026-09-25T09:00:00Z',
    };
    mockUpdateTicket.mockResolvedValue(updatedTicket);

    renderTicketDetailsPage();
    await waitForTicketToLoad();

    fireEvent.click(screen.getByRole('button', { name: 'Edit Ticket' }));
    fireEvent.change(screen.getByLabelText(/Title/i), { target: { value: 'Updated title' } });
    fireEvent.change(screen.getByLabelText(/Description/i), {
      target: { value: 'Updated description.' },
    });
    fireEvent.change(screen.getByLabelText(/Priority/i), { target: { value: 'CRITICAL' } });
    fireEvent.change(screen.getByLabelText(/Assignee/i), { target: { value: 'jane.doe' } });
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() => {
      expect(mockUpdateTicket).toHaveBeenCalledWith(42, {
        title: 'Updated title',
        description: 'Updated description.',
        priority: 'CRITICAL',
        assignee: 'jane.doe',
      });
    });

    const payload = mockUpdateTicket.mock.calls[0][1];
    expect(payload).not.toHaveProperty('status');
    expect(payload).not.toHaveProperty('id');
    expect(payload).not.toHaveProperty('createdAt');
    expect(payload).not.toHaveProperty('updatedAt');
  });

  it('prevents duplicate save requests while saving', async () => {
    let resolveUpdate: (value: Ticket) => void = () => {};
    mockUpdateTicket.mockImplementation(
      () =>
        new Promise<Ticket>((resolve) => {
          resolveUpdate = resolve;
        }),
    );

    renderTicketDetailsPage();
    await waitForTicketToLoad();

    fireEvent.click(screen.getByRole('button', { name: 'Edit Ticket' }));
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));

    expect(screen.getByRole('button', { name: 'Saving...' })).toBeDisabled();

    fireEvent.click(screen.getByRole('button', { name: 'Saving...' }));
    expect(mockUpdateTicket).toHaveBeenCalledTimes(1);

    resolveUpdate({
      ...baseTicket,
      title: 'Updated title',
      updatedAt: '2026-09-25T09:00:00Z',
    });

    await waitFor(() => {
      expect(screen.getByText('Ticket updated successfully.')).toBeInTheDocument();
    });
  });

  it('displays updated values after a successful save', async () => {
    mockUpdateTicket.mockResolvedValue({
      ...baseTicket,
      title: 'Updated title',
      description: 'Updated description.',
      priority: 'CRITICAL',
      assignee: 'jane.doe',
      updatedAt: '2026-09-25T09:00:00Z',
    });

    renderTicketDetailsPage();
    await waitForTicketToLoad();

    fireEvent.click(screen.getByRole('button', { name: 'Edit Ticket' }));
    fireEvent.change(screen.getByLabelText(/Title/i), { target: { value: 'Updated title' } });
    fireEvent.change(screen.getByLabelText(/Description/i), {
      target: { value: 'Updated description.' },
    });
    fireEvent.change(screen.getByLabelText(/Priority/i), { target: { value: 'CRITICAL' } });
    fireEvent.change(screen.getByLabelText(/Assignee/i), { target: { value: 'jane.doe' } });
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() => {
      expect(screen.getByText('Updated title')).toBeInTheDocument();
      expect(screen.getByText('Updated description.')).toBeInTheDocument();
      expect(screen.getByText('CRITICAL')).toBeInTheDocument();
      expect(screen.getByText('jane.doe')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Edit Ticket' })).toBeInTheDocument();
    });
  });

  it('discards unsaved changes when cancel is selected', async () => {
    renderTicketDetailsPage();
    await waitForTicketToLoad();

    fireEvent.click(screen.getByRole('button', { name: 'Edit Ticket' }));
    fireEvent.change(screen.getByLabelText(/Title/i), { target: { value: 'Unsaved title' } });
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));

    expect(screen.getByText('Unable to login')).toBeInTheDocument();
    expect(screen.queryByLabelText(/Title/i)).not.toBeInTheDocument();
    expect(mockUpdateTicket).not.toHaveBeenCalled();
  });

  it('displays backend validation errors for HTTP 400', async () => {
    mockUpdateTicket.mockRejectedValue(
      new ApiError(400, {
        status: 400,
        code: 'INVALID_REQUEST',
        message: 'Title must not be blank',
      }),
    );

    renderTicketDetailsPage();
    await waitForTicketToLoad();

    fireEvent.click(screen.getByRole('button', { name: 'Edit Ticket' }));
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('Title must not be blank')).toBeInTheDocument();
  });

  it('displays a meaningful message for HTTP 500 update failures', async () => {
    mockUpdateTicket.mockRejectedValue(
      new ApiError(500, {
        status: 500,
        code: 'INTERNAL_SERVER_ERROR',
        message: 'An unexpected error occurred.',
      }),
    );

    renderTicketDetailsPage();
    await waitForTicketToLoad();

    fireEvent.click(screen.getByRole('button', { name: 'Edit Ticket' }));
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));

    expect(
      await screen.findByText('Unable to update ticket. Please try again.'),
    ).toBeInTheDocument();
  });

  it('navigates back to the ticket list', async () => {
    renderTicketDetailsPage();
    await waitForTicketToLoad();

    fireEvent.click(screen.getByRole('link', { name: 'Back to tickets' }));

    expect(screen.getByRole('heading', { name: 'Tickets' })).toBeInTheDocument();
  });

  it('displays comments loaded with the ticket', async () => {
    mockGetTicket.mockResolvedValue({
      ...baseTicket,
      comments: [
        {
          id: 1,
          ticketId: 42,
          content: 'Investigating the issue.',
          author: 'jane.doe',
          createdAt: '2026-09-25T09:00:00Z',
        },
      ],
    });

    renderTicketDetailsPage();
    await waitForTicketToLoad();

    expect(screen.getByRole('heading', { name: 'Comments' })).toBeInTheDocument();
    expect(screen.getByText('Investigating the issue.')).toBeInTheDocument();
    expect(screen.getByText('jane.doe')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Edit Ticket' })).toBeInTheDocument();
  });

  it('displays an empty comments state after the ticket loads', async () => {
    renderTicketDetailsPage();
    await waitForTicketToLoad();

    expect(screen.getByText('No comments yet.')).toBeInTheDocument();
  });

  it('displays a comments load error and retries through ticket reload', async () => {
    mockGetTicket
      .mockRejectedValueOnce(
        new ApiError(500, {
          status: 500,
          code: 'INTERNAL_SERVER_ERROR',
          message: 'An unexpected error occurred.',
        }),
      )
      .mockResolvedValueOnce(baseTicket);

    renderTicketDetailsPage();

    expect(await screen.findByText('Unable to load comments. Please try again.')).toBeInTheDocument();

    fireEvent.click(screen.getAllByRole('button', { name: 'Try again' })[0]);

    await waitForTicketToLoad();
    expect(mockGetTicket).toHaveBeenCalledTimes(2);
  });

  it('displays status transition actions on the ticket details page', async () => {
    renderTicketDetailsPage();
    await waitForTicketToLoad();

    expect(screen.getByRole('heading', { name: 'Status Actions' })).toBeInTheDocument();
    expect(screen.getByText(/Current status:/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Start Progress' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cancel Ticket' })).toBeInTheDocument();
  });

  it('updates displayed status after a successful transition', async () => {
    mockTransitionTicketStatus.mockResolvedValue({
      ...baseTicket,
      status: 'IN_PROGRESS',
      updatedAt: '2026-09-25T09:00:00Z',
    });

    renderTicketDetailsPage();
    await waitForTicketToLoad();

    fireEvent.click(screen.getByRole('button', { name: 'Start Progress' }));

    await waitFor(() => {
      const statusSection = screen.getByRole('heading', { name: 'Status Actions' }).closest('section');
      expect(statusSection).not.toBeNull();
      expect(within(statusSection as HTMLElement).getByText('IN_PROGRESS')).toBeInTheDocument();
      expect(screen.getByText('Ticket status updated successfully.')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Resolve' })).toBeInTheDocument();
    });

    expect(mockTransitionTicketStatus).toHaveBeenCalledWith(42, { status: 'IN_PROGRESS' });
  });

  it('keeps status transition actions available while editing other fields', async () => {
    renderTicketDetailsPage();
    await waitForTicketToLoad();

    fireEvent.click(screen.getByRole('button', { name: 'Edit Ticket' }));

    expect(screen.getByRole('heading', { name: 'Status Actions' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Start Progress' })).toBeInTheDocument();
  });

  it('adds a comment while keeping ticket details visible', async () => {
    mockAddComment.mockResolvedValue({
      id: 2,
      ticketId: 42,
      content: 'I have investigated the issue.',
      author: 'john.doe',
      createdAt: '2026-09-25T11:30:00Z',
    });

    renderTicketDetailsPage();
    await waitForTicketToLoad();

    fireEvent.change(screen.getByLabelText('Comment'), {
      target: { value: 'I have investigated the issue.' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Add Comment' }));

    await waitFor(() => {
      expect(screen.getByText('I have investigated the issue.')).toBeInTheDocument();
      expect(screen.getByText('Unable to login')).toBeInTheDocument();
    });
  });
});
