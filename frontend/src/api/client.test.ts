import { afterEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from './client';
import { ApiError } from '../types/api';

describe('apiClient', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('uses the configured API base URL for GET requests', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => [],
    });
    vi.stubGlobal('fetch', fetchMock);

    await apiClient.get('/tickets');

    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/v1/tickets',
      expect.objectContaining({ method: 'GET' }),
    );
  });

  it('preserves backend error information for non-2xx responses', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => ({
        status: 400,
        code: 'VALIDATION_ERROR',
        message: 'Request validation failed.',
        path: '/api/v1/tickets',
        fieldErrors: { title: 'Title must not be blank' },
      }),
    });
    vi.stubGlobal('fetch', fetchMock);

    await expect(apiClient.post('/tickets', {})).rejects.toMatchObject({
      name: 'ApiError',
      status: 400,
      code: 'VALIDATION_ERROR',
      message: 'Request validation failed.',
      path: '/api/v1/tickets',
      fieldErrors: { title: 'Title must not be blank' },
    } satisfies Partial<ApiError>);
  });
});
