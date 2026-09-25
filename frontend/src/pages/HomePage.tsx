import { Link } from 'react-router-dom';

export function HomePage() {
  return (
    <section className="home-page">
      <div className="page-hero">
        <h2>Welcome</h2>
        <p>
          Manage support tickets from creation through resolution. Search and filter tickets,
          update details, add comments, and transition status according to the workflow.
        </p>
        <div className="page-hero-actions">
          <Link to="/tickets" className="button-primary">
            View Tickets
          </Link>
          <Link to="/tickets/new" className="button-link">
            Create Ticket
          </Link>
        </div>
      </div>

      <div className="info-cards">
        <article className="info-card">
          <h3>Track tickets</h3>
          <p>List, search, and filter tickets by keyword or status.</p>
        </article>
        <article className="info-card">
          <h3>Collaborate</h3>
          <p>Add comments and assign tickets to team members.</p>
        </article>
        <article className="info-card">
          <h3>Follow workflow</h3>
          <p>Move tickets through OPEN → IN_PROGRESS → RESOLVED → CLOSED.</p>
        </article>
      </div>
    </section>
  );
}
