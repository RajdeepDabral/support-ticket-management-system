import type { TicketStatus } from '../types/ticket';

const ALLOWED_TRANSITIONS: Record<TicketStatus, TicketStatus[]> = {
  OPEN: ['IN_PROGRESS', 'CANCELLED'],
  IN_PROGRESS: ['RESOLVED', 'CANCELLED'],
  RESOLVED: ['CLOSED'],
  CLOSED: [],
  CANCELLED: [],
};

const TRANSITION_ACTION_LABELS: Record<TicketStatus, Partial<Record<TicketStatus, string>>> = {
  OPEN: {
    IN_PROGRESS: 'Start Progress',
    CANCELLED: 'Cancel Ticket',
  },
  IN_PROGRESS: {
    RESOLVED: 'Resolve',
    CANCELLED: 'Cancel Ticket',
  },
  RESOLVED: {
    CLOSED: 'Close',
  },
  CLOSED: {},
  CANCELLED: {},
};

export function getAllowedTransitions(currentStatus: TicketStatus): TicketStatus[] {
  return ALLOWED_TRANSITIONS[currentStatus];
}

export function getTransitionActionLabel(
  currentStatus: TicketStatus,
  targetStatus: TicketStatus,
): string {
  return TRANSITION_ACTION_LABELS[currentStatus][targetStatus] ?? targetStatus;
}

export function isCancelTransition(targetStatus: TicketStatus): boolean {
  return targetStatus === 'CANCELLED';
}
