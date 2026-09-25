import { afterEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from './client';
import { listTickets } from './tickets';

vi.mock('./client', () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

const mockGet = vi.mocked(apiClient.get);

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
