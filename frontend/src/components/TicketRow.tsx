import { Link } from 'react-router-dom';
import { formatDateTime } from '../lib/formatDateTime';
import type { Ticket } from '../types/ticket';
import { PriorityBadge } from './PriorityBadge';
import { StatusBadge } from './StatusBadge';

interface TicketRowProps {
  ticket: Ticket;
}

export function TicketRow({ ticket }: TicketRowProps) {
  return (
    <tr>
      <td>{ticket.id}</td>
      <td className="ticket-table-title">{ticket.title}</td>
      <td>
        <PriorityBadge priority={ticket.priority} />
      </td>
      <td>
        <StatusBadge status={ticket.status} />
      </td>
      <td>{ticket.assignee}</td>
      <td>{formatDateTime(ticket.updatedAt)}</td>
      <td>
        <Link to={`/tickets/${ticket.id}`} className="table-action-link">
          View
        </Link>
      </td>
    </tr>
  );
}
