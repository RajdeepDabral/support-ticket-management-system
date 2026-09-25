export type TicketStatus =
  | 'OPEN'
  | 'IN_PROGRESS'
  | 'RESOLVED'
  | 'CLOSED'
  | 'CANCELLED';

export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type TicketStatusFilter = TicketStatus | 'ALL';

export interface Ticket {
  id: number;
  title: string;
  description: string;
  priority: TicketPriority;
  status: TicketStatus;
  assignee: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTicketRequest {
  title: string;
  description: string;
  priority: TicketPriority;
  assignee: string;
}

export interface ListTicketsParams {
  keyword?: string;
  status?: TicketStatus;
}

export interface Comment {
  id: number;
  ticketId: number;
  content: string;
  author: string;
  createdAt: string;
}

export interface TicketDetail extends Ticket {
  comments: Comment[];
}

export interface UpdateTicketRequest {
  title?: string;
  description?: string;
  priority?: TicketPriority;
  assignee?: string;
}

export const TICKET_PRIORITY_OPTIONS: TicketPriority[] = [
  'LOW',
  'MEDIUM',
  'HIGH',
  'CRITICAL',
];

export const TICKET_STATUS_OPTIONS: TicketStatus[] = [
  'OPEN',
  'IN_PROGRESS',
  'RESOLVED',
  'CLOSED',
  'CANCELLED',
];
