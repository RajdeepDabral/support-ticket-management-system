import type { APIRequestContext } from '@playwright/test';

const API_BASE_URL = process.env.E2E_API_BASE_URL ?? 'http://localhost:8080/api/v1';

export interface ApiTicket {
  id: number;
  title: string;
  description: string;
  priority: string;
  status: string;
  assignee: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApiTicketDetail extends ApiTicket {
  comments: ApiComment[];
}

export interface ApiComment {
  id: number;
  ticketId: number;
  content: string;
  author: string;
  createdAt: string;
}

export interface ApiErrorBody {
  status?: number;
  code?: string;
  message?: string;
}

export interface CreateTicketPayload {
  title: string;
  description: string;
  priority: string;
  assignee: string;
}

function ticketsUrl(path = ''): string {
  return `${API_BASE_URL}/tickets${path}`;
}

export async function listTickets(request: APIRequestContext): Promise<ApiTicket[]> {
  const response = await request.get(ticketsUrl());
  if (!response.ok()) {
    throw new Error(`Failed to list tickets: ${response.status()}`);
  }

  return (await response.json()) as ApiTicket[];
}

export async function createTicket(
  request: APIRequestContext,
  payload: CreateTicketPayload,
): Promise<ApiTicket> {
  const response = await request.post(ticketsUrl(), {
    data: payload,
  });

  if (!response.ok()) {
    const body = (await response.json()) as ApiErrorBody;
    throw new Error(`Failed to create ticket: ${response.status()} ${body.message ?? ''}`);
  }

  return (await response.json()) as ApiTicket;
}

export async function getTicket(
  request: APIRequestContext,
  ticketId: number,
): Promise<ApiTicketDetail> {
  const response = await request.get(ticketsUrl(`/${ticketId}`));

  if (!response.ok()) {
    const body = (await response.json()) as ApiErrorBody;
    throw new Error(`Failed to get ticket ${ticketId}: ${response.status()} ${body.message ?? ''}`);
  }

  return (await response.json()) as ApiTicketDetail;
}

export async function updateTicket(
  request: APIRequestContext,
  ticketId: number,
  payload: Record<string, string>,
): Promise<ApiTicket> {
  const response = await request.patch(ticketsUrl(`/${ticketId}`), {
    data: payload,
  });

  if (!response.ok()) {
    const body = (await response.json()) as ApiErrorBody;
    throw new Error(`Failed to update ticket ${ticketId}: ${response.status()} ${body.message ?? ''}`);
  }

  return (await response.json()) as ApiTicket;
}

export async function transitionTicketStatus(
  request: APIRequestContext,
  ticketId: number,
  status: string,
): Promise<ApiTicket> {
  const response = await request.patch(ticketsUrl(`/${ticketId}/status`), {
    data: { status },
  });

  if (!response.ok()) {
    const body = (await response.json()) as ApiErrorBody;
    throw new Error(
      `Failed to transition ticket ${ticketId} to ${status}: ${response.status()} ${body.message ?? ''}`,
    );
  }

  return (await response.json()) as ApiTicket;
}

export async function attemptTransition(
  request: APIRequestContext,
  ticketId: number,
  status: string,
): Promise<{ status: number; body: ApiErrorBody }> {
  const response = await request.patch(ticketsUrl(`/${ticketId}/status`), {
    data: { status },
  });

  const body = (await response.json()) as ApiErrorBody;
  return { status: response.status(), body };
}

export async function addComment(
  request: APIRequestContext,
  ticketId: number,
  content: string,
  author: string,
): Promise<ApiComment> {
  const response = await request.post(ticketsUrl(`/${ticketId}/comments`), {
    data: { content, author },
  });

  if (!response.ok()) {
    const body = (await response.json()) as ApiErrorBody;
    throw new Error(`Failed to add comment: ${response.status()} ${body.message ?? ''}`);
  }

  return (await response.json()) as ApiComment;
}

export async function waitForBackend(request: APIRequestContext, attempts = 60): Promise<void> {
  for (let attempt = 0; attempt < attempts; attempt += 1) {
    const response = await request.get(ticketsUrl());
    if (response.ok()) {
      return;
    }

    await new Promise((resolve) => {
      setTimeout(resolve, 1_000);
    });
  }

  throw new Error(`Backend did not become ready at ${API_BASE_URL}`);
}
