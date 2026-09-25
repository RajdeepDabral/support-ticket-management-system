import { useState } from 'react';
import { transitionTicketStatus } from '../api/tickets';
import {
  getAllowedTransitions,
  getTransitionActionLabel,
  isCancelTransition,
} from '../lib/ticketStatusTransitions';
import { ApiError } from '../types/api';
import type { Ticket, TicketStatus } from '../types/ticket';

const TRANSITION_ERROR_MESSAGE = 'Unable to change ticket status. Please try again.';
const TRANSITION_CONFLICT_MESSAGE =
  'Unable to change ticket status. The requested transition is not allowed.';
const NETWORK_ERROR_MESSAGE =
  'Unable to connect to the server. Please check your connection and try again.';

interface TicketStatusActionsProps {
  ticketId: number;
  currentStatus: TicketStatus;
  onTransitionSuccess: (ticket: Ticket) => void;
  onTicketNotFound: () => void;
}

function mapTransitionApiError(error: ApiError): string {
  if (error.status === 409) {
    return error.message ?? TRANSITION_CONFLICT_MESSAGE;
  }

  if (error.status === 400 && error.message) {
    return error.message;
  }

  return TRANSITION_ERROR_MESSAGE;
}

export function TicketStatusActions({
  ticketId,
  currentStatus,
  onTransitionSuccess,
  onTicketNotFound,
}: TicketStatusActionsProps) {
  const [isTransitioning, setIsTransitioning] = useState(false);
  const [transitioningTo, setTransitioningTo] = useState<TicketStatus | null>(null);
  const [transitionError, setTransitionError] = useState<string | null>(null);

  const allowedTransitions = getAllowedTransitions(currentStatus);

  const handleTransition = async (targetStatus: TicketStatus) => {
    if (isTransitioning) {
      return;
    }

    if (isCancelTransition(targetStatus)) {
      const confirmed = window.confirm('Cancel this ticket?');
      if (!confirmed) {
        return;
      }
    }

    setIsTransitioning(true);
    setTransitioningTo(targetStatus);
    setTransitionError(null);

    try {
      const updatedTicket = await transitionTicketStatus(ticketId, { status: targetStatus });
      onTransitionSuccess(updatedTicket);
    } catch (error) {
      if (error instanceof ApiError) {
        if (error.status === 404) {
          onTicketNotFound();
        } else {
          setTransitionError(mapTransitionApiError(error));
        }
      } else {
        setTransitionError(NETWORK_ERROR_MESSAGE);
      }
    } finally {
      setIsTransitioning(false);
      setTransitioningTo(null);
    }
  };

  return (
    <section className="ticket-status-actions" aria-labelledby="ticket-status-actions-heading">
      <h3 id="ticket-status-actions-heading">Status Actions</h3>

      <p className="ticket-status-current">
        Current status: <strong>{currentStatus}</strong>
      </p>

      {transitionError && (
        <div className="ticket-status-transition-error" role="alert">
          <p>{transitionError}</p>
        </div>
      )}

      {allowedTransitions.length === 0 ? (
        <p className="ticket-status-no-actions" role="status">No status actions available.</p>
      ) : (
        <div className="ticket-status-action-buttons">
          {isTransitioning && (
            <p className="ticket-status-transition-loading" role="status">Updating status...</p>
          )}
          {allowedTransitions.map((targetStatus) => {
            const label = getTransitionActionLabel(currentStatus, targetStatus);

            return (
              <button
                key={targetStatus}
                type="button"
                disabled={isTransitioning}
                aria-busy={isTransitioning && transitioningTo === targetStatus}
                onClick={() => {
                  void handleTransition(targetStatus);
                }}
              >
                {label}
              </button>
            );
          })}
        </div>
      )}
    </section>
  );
}

export {
  NETWORK_ERROR_MESSAGE as STATUS_TRANSITION_NETWORK_ERROR_MESSAGE,
  TRANSITION_CONFLICT_MESSAGE,
  TRANSITION_ERROR_MESSAGE,
};
