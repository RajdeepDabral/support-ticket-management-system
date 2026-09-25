import { Link } from 'react-router-dom';
import { formatDateTime } from '../lib/formatDateTime';
import type { Ticket } from '../types/ticket';

interface TicketRowProps {
  ticket: Ticket;
}

export function TicketRow({ ticket }: TicketRowProps) {
  return (
    <tr>
      <td>{ticket.id}</td>
      <td>{ticket.title}</td>
      <td>{ticket.priority}</td>
      <td>{ticket.status}</td>
      <td>{ticket.assignee}</td>
      <td>{formatDateTime(ticket.updatedAt)}</td>
      <td>
        <Link to={`/tickets/${ticket.id}`}>View</Link>
      </td>
    </tr>
  );
}
