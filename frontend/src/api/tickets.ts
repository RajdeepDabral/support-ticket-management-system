import { apiClient } from './client';
import type {
  Comment,
  CreateCommentRequest,
  CreateTicketRequest,
  ListTicketsParams,
  Ticket,
  TicketDetail,
  UpdateTicketRequest,
  UpdateTicketStatusRequest,
} from '../types/ticket';

function buildListTicketsPath(params?: ListTicketsParams): string {
  const searchParams = new URLSearchParams();

  if (params?.keyword) {
    const trimmedKeyword = params.keyword.trim();
    if (trimmedKeyword) {
      searchParams.set('keyword', trimmedKeyword);
    }
  }

  if (params?.status) {
    searchParams.set('status', params.status);
  }

  const query = searchParams.toString();
  return query ? `/tickets?${query}` : '/tickets';
}

export function listTickets(params?: ListTicketsParams): Promise<Ticket[]> {
  return apiClient.get<Ticket[]>(buildListTicketsPath(params));
}

export function createTicket(request: CreateTicketRequest): Promise<Ticket> {
  return apiClient.post<Ticket>('/tickets', request);
}

export function getTicket(ticketId: number): Promise<TicketDetail> {
  return apiClient.get<TicketDetail>(`/tickets/${ticketId}`);
}

export function updateTicket(ticketId: number, request: UpdateTicketRequest): Promise<Ticket> {
  return apiClient.patch<Ticket>(`/tickets/${ticketId}`, request);
}

export async function getComments(ticketId: number): Promise<Comment[]> {
  const ticket = await getTicket(ticketId);
  return ticket.comments ?? [];
}

export function addComment(ticketId: number, request: CreateCommentRequest): Promise<Comment> {
  return apiClient.post<Comment>(`/tickets/${ticketId}/comments`, request);
}

export function transitionTicketStatus(
  ticketId: number,
  request: UpdateTicketStatusRequest,
): Promise<Ticket> {
  return apiClient.patch<Ticket>(`/tickets/${ticketId}/status`, request);
}
