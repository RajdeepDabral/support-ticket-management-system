import { afterEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from './client';
import { addComment, createTicket, getComments, getTicket, listTickets, updateTicket } from './tickets';

vi.mock('./client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn(),
  },
}));

const mockGet = vi.mocked(apiClient.get);
const mockPost = vi.mocked(apiClient.post);
const mockPatch = vi.mocked(apiClient.patch);

describe('listTickets', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('requests /tickets without query parameters by default', async () => {
    mockGet.mockResolvedValue([]);

    await listTickets();

    expect(mockGet).toHaveBeenCalledWith('/tickets');
  });

  it('builds keyword and status query parameters', async () => {
    mockGet.mockResolvedValue([]);

    await listTickets({ keyword: 'login', status: 'OPEN' });

    expect(mockGet).toHaveBeenCalledWith('/tickets?keyword=login&status=OPEN');
  });

  it('omits blank keywords and does not send status for unfiltered requests', async () => {
    mockGet.mockResolvedValue([]);

    await listTickets({ keyword: '   ' });

    expect(mockGet).toHaveBeenCalledWith('/tickets');
  });
});

describe('createTicket', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('posts only the allowed create-ticket fields to /tickets', async () => {
    mockPost.mockResolvedValue({
      id: 1,
      title: 'Unable to login',
      description: 'User cannot login.',
      priority: 'HIGH',
      status: 'OPEN',
      assignee: 'john.doe',
      createdAt: '2026-09-25T08:00:00Z',
      updatedAt: '2026-09-25T08:00:00Z',
    });

    await createTicket({
      title: 'Unable to login',
      description: 'User cannot login.',
      priority: 'HIGH',
      assignee: 'john.doe',
    });

    expect(mockPost).toHaveBeenCalledWith('/tickets', {
      title: 'Unable to login',
      description: 'User cannot login.',
      priority: 'HIGH',
      assignee: 'john.doe',
    });
  });
});

describe('getTicket', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('requests GET /tickets/{ticketId}', async () => {
    mockGet.mockResolvedValue({
      id: 42,
      title: 'Unable to login',
      description: 'User cannot login.',
      priority: 'HIGH',
      status: 'OPEN',
      assignee: 'john.doe',
      createdAt: '2026-09-25T08:00:00Z',
      updatedAt: '2026-09-25T08:00:00Z',
      comments: [],
    });

    await getTicket(42);

    expect(mockGet).toHaveBeenCalledWith('/tickets/42');
  });
});

describe('updateTicket', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('sends PATCH /tickets/{ticketId} with only editable fields', async () => {
    mockPatch.mockResolvedValue({
      id: 42,
      title: 'Updated title',
      description: 'Updated description.',
      priority: 'CRITICAL',
      status: 'OPEN',
      assignee: 'jane.doe',
      createdAt: '2026-09-25T08:00:00Z',
      updatedAt: '2026-09-25T09:00:00Z',
    });

    await updateTicket(42, {
      title: 'Updated title',
      description: 'Updated description.',
      priority: 'CRITICAL',
      assignee: 'jane.doe',
    });

    expect(mockPatch).toHaveBeenCalledWith('/tickets/42', {
      title: 'Updated title',
      description: 'Updated description.',
      priority: 'CRITICAL',
      assignee: 'jane.doe',
    });
  });
});

describe('getComments', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('retrieves comments from GET /tickets/{ticketId}', async () => {
    mockGet.mockResolvedValue({
      id: 42,
      title: 'Unable to login',
      description: 'User cannot login.',
      priority: 'HIGH',
      status: 'OPEN',
      assignee: 'john.doe',
      createdAt: '2026-09-25T08:00:00Z',
      updatedAt: '2026-09-25T08:00:00Z',
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

    const comments = await getComments(42);

    expect(mockGet).toHaveBeenCalledWith('/tickets/42');
    expect(comments).toHaveLength(1);
    expect(comments[0].content).toBe('Investigating the issue.');
  });
});

describe('addComment', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('posts content and author to /tickets/{ticketId}/comments', async () => {
    mockPost.mockResolvedValue({
      id: 10,
      ticketId: 42,
      content: 'I have investigated the issue.',
      author: 'john.doe',
      createdAt: '2026-09-25T11:30:00Z',
    });

    await addComment(42, {
      content: 'I have investigated the issue.',
      author: 'john.doe',
    });

    expect(mockPost).toHaveBeenCalledWith('/tickets/42/comments', {
      content: 'I have investigated the issue.',
      author: 'john.doe',
    });
  });
});
