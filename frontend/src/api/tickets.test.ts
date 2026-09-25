import { afterEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from './client';
import { createTicket, listTickets } from './tickets';

vi.mock('./client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

const mockGet = vi.mocked(apiClient.get);
const mockPost = vi.mocked(apiClient.post);

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
