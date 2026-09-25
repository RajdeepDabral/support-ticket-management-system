import { getApiBaseUrl } from '../lib/env';
import { ApiError, type ApiErrorBody } from '../types/api';

type HttpMethod = 'GET' | 'POST' | 'PATCH';

function buildUrl(path: string): string {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return `${getApiBaseUrl()}${normalizedPath}`;
}

async function parseErrorBody(response: Response): Promise<ApiErrorBody | undefined> {
  const contentType = response.headers.get('content-type');

  if (!contentType?.includes('application/json')) {
    return undefined;
  }

  try {
    return (await response.json()) as ApiErrorBody;
  } catch {
    return undefined;
  }
}

async function request<T>(method: HttpMethod, path: string, body?: unknown): Promise<T> {
  const response = await fetch(buildUrl(path), {
    method,
    headers: body !== undefined ? { 'Content-Type': 'application/json' } : undefined,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!response.ok) {
    const errorBody = await parseErrorBody(response);
    throw new ApiError(response.status, errorBody);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get('content-type');
  if (!contentType?.includes('application/json')) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export const apiClient = {
  get: <T>(path: string): Promise<T> => request<T>('GET', path),
  post: <T>(path: string, body: unknown): Promise<T> => request<T>('POST', path, body),
  patch: <T>(path: string, body: unknown): Promise<T> => request<T>('PATCH', path, body),
};
