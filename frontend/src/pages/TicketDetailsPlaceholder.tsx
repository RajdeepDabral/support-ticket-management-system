import { Link, useParams } from 'react-router-dom';

export function TicketDetailsPlaceholder() {
  const { ticketId } = useParams();

  return (
    <section className="ticket-details-placeholder">
      <h2>Ticket Details</h2>
      <p>Ticket details for ticket {ticketId} will be implemented in a later step.</p>
      <Link to="/tickets">Back to tickets</Link>
    </section>
  );
}
