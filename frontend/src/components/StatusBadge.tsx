import type { TicketStatus } from '../types/ticket';

interface StatusBadgeProps {
  status: TicketStatus;
}

export function StatusBadge({ status }: StatusBadgeProps) {
  return (
    <span className={`status-badge status-badge--${status}`}>
      {status.replace('_', ' ')}
    </span>
  );
}
