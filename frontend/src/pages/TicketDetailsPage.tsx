import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getTicket, updateTicket } from '../api/tickets';
import { TicketEditForm, type TicketEditField } from '../components/TicketEditForm';
import {
  COMMENTS_LOAD_ERROR_MESSAGE,
  TicketCommentsSection,
} from '../components/TicketCommentsSection';
import { formatDateTime } from '../lib/formatDateTime';
import { ApiError } from '../types/api';
import type { Comment, TicketDetail, UpdateTicketRequest } from '../types/ticket';

const LOAD_ERROR_MESSAGE = 'Unable to load ticket. Please try again.';
const NETWORK_ERROR_MESSAGE =
  'Unable to connect to the server. Please check your connection and try again.';
const UPDATE_ERROR_MESSAGE = 'Unable to update ticket. Please try again.';
const NOT_FOUND_MESSAGE = 'Ticket not found.';
const UPDATE_SUCCESS_MESSAGE = 'Ticket updated successfully.';

function mapUpdateApiError(error: ApiError): {
  fieldErrors: Partial<Record<TicketEditField, string>>;
  formError: string | null;
} {
  const fieldErrors: Partial<Record<TicketEditField, string>> = {};

  if (error.fieldErrors) {
    for (const [field, message] of Object.entries(error.fieldErrors)) {
      if (field === 'title' || field === 'description' || field === 'priority' || field === 'assignee') {
        fieldErrors[field] = message;
      }
    }
  }

  const message = error.message ?? '';

  if (message.includes('Title must not be blank') || message.includes('Title must not exceed')) {
    fieldErrors.title = message;
  } else if (message.includes('Description must not be blank')) {
    fieldErrors.description = message;
  } else if (message.includes('Assignee must not be blank') || message.includes('Assignee must not exceed')) {
    fieldErrors.assignee = message;
  } else if (message.includes('Priority must be one of')) {
    fieldErrors.priority = message;
  }

  if (Object.keys(fieldErrors).length > 0) {
    return { fieldErrors, formError: null };
  }

  if (error.status === 400 && message) {
    return { fieldErrors, formError: message };
  }

  return { fieldErrors, formError: UPDATE_ERROR_MESSAGE };
}

function parseTicketId(rawTicketId: string | undefined): number | null {
  if (!rawTicketId) {
    return null;
  }

  const ticketId = Number(rawTicketId);
  if (!Number.isInteger(ticketId) || ticketId <= 0) {
    return null;
  }

  return ticketId;
}

export function TicketDetailsPage() {
  const { ticketId: rawTicketId } = useParams();
  const ticketId = parseTicketId(rawTicketId);

  const [ticket, setTicket] = useState<TicketDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [isNotFound, setIsNotFound] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<TicketEditField, string>>>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [saveSuccessMessage, setSaveSuccessMessage] = useState<string | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);

  const loadTicket = useCallback(async () => {
    if (ticketId === null) {
      setTicket(null);
      setIsLoading(false);
      setLoadError(null);
      setIsNotFound(true);
      return;
    }

    setIsLoading(true);
    setLoadError(null);
    setIsNotFound(false);

    try {
      const data = await getTicket(ticketId);
      setTicket(data);
      setComments(data.comments ?? []);
    } catch (error) {
      setTicket(null);
      setComments([]);

      if (error instanceof ApiError && error.status === 404) {
        setIsNotFound(true);
        setLoadError(null);
      } else {
        setIsNotFound(false);
        setLoadError(
          error instanceof ApiError ? LOAD_ERROR_MESSAGE : NETWORK_ERROR_MESSAGE,
        );
      }
    } finally {
      setIsLoading(false);
    }
  }, [ticketId]);

  useEffect(() => {
    setIsEditing(false);
    setFieldErrors({});
    setFormError(null);
    setSaveSuccessMessage(null);
    setComments([]);
    void loadTicket();
  }, [loadTicket]);

  const handleEdit = () => {
    setIsEditing(true);
    setFieldErrors({});
    setFormError(null);
    setSaveSuccessMessage(null);
  };

  const handleCancel = () => {
    setIsEditing(false);
    setFieldErrors({});
    setFormError(null);
  };

  const handleSave = async (request: UpdateTicketRequest) => {
    if (ticketId === null || ticket === null || isSaving) {
      return;
    }

    setIsSaving(true);
    setFieldErrors({});
    setFormError(null);
    setSaveSuccessMessage(null);

    try {
      const updatedTicket = await updateTicket(ticketId, request);
      setTicket({ ...updatedTicket, comments: ticket.comments });
      setIsEditing(false);
      setSaveSuccessMessage(UPDATE_SUCCESS_MESSAGE);
    } catch (error) {
      if (error instanceof ApiError) {
        if (error.status === 404) {
          setIsEditing(false);
          setIsNotFound(true);
          setTicket(null);
          setComments([]);
        } else {
          const mapped = mapUpdateApiError(error);
          setFieldErrors(mapped.fieldErrors);
          setFormError(mapped.formError);
        }
      } else {
        setFormError(NETWORK_ERROR_MESSAGE);
      }
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <section className="ticket-details-page">
      <div className="ticket-details-header">
        <h2>Ticket Details</h2>
        <Link to="/tickets" className="button-link">Back to tickets</Link>
      </div>

      {isLoading && (
        <p className="ticket-details-loading" role="status">Loading ticket...</p>
      )}

      {!isLoading && isNotFound && (
        <div className="ticket-details-not-found" role="alert">
          <p>{NOT_FOUND_MESSAGE}</p>
          <Link to="/tickets" className="button-link">Return to ticket list</Link>
        </div>
      )}

      {!isLoading && loadError && (
        <div className="ticket-details-error" role="alert">
          <p>{loadError}</p>
          <button type="button" onClick={() => void loadTicket()}>Try again</button>
        </div>
      )}

      {!isLoading && ticket && !isNotFound && !loadError && (
        <>
          {saveSuccessMessage && (
            <div className="ticket-details-success" role="status">
              <p>{saveSuccessMessage}</p>
            </div>
          )}

          {isEditing ? (
            <TicketEditForm
              ticket={ticket}
              isSaving={isSaving}
              fieldErrors={fieldErrors}
              formError={formError}
              onSave={(request) => {
                void handleSave(request);
              }}
              onCancel={handleCancel}
            />
          ) : (
            <div className="ticket-details-view">
              <dl className="ticket-details-meta">
                <div className="ticket-details-meta-item">
                  <dt>Ticket ID</dt>
                  <dd>{ticket.id}</dd>
                </div>
                <div className="ticket-details-meta-item">
                  <dt>Title</dt>
                  <dd>{ticket.title}</dd>
                </div>
                <div className="ticket-details-meta-item ticket-details-meta-item--full">
                  <dt>Description</dt>
                  <dd>{ticket.description}</dd>
                </div>
                <div className="ticket-details-meta-item">
                  <dt>Priority</dt>
                  <dd>{ticket.priority}</dd>
                </div>
                <div className="ticket-details-meta-item">
                  <dt>Status</dt>
                  <dd>{ticket.status}</dd>
                </div>
                <div className="ticket-details-meta-item">
                  <dt>Assignee</dt>
                  <dd>{ticket.assignee}</dd>
                </div>
                <div className="ticket-details-meta-item">
                  <dt>Created</dt>
                  <dd>{formatDateTime(ticket.createdAt)}</dd>
                </div>
                <div className="ticket-details-meta-item">
                  <dt>Updated</dt>
                  <dd>{formatDateTime(ticket.updatedAt)}</dd>
                </div>
              </dl>

              <div className="ticket-details-actions">
                <button type="button" onClick={handleEdit}>Edit Ticket</button>
              </div>
            </div>
          )}
        </>
      )}

      {ticketId !== null && !isNotFound && (
        <TicketCommentsSection
          ticketId={ticketId}
          author={ticket?.assignee ?? ''}
          comments={comments}
          isLoadingComments={isLoading}
          commentsLoadError={
            !isLoading && loadError
              ? loadError === NETWORK_ERROR_MESSAGE
                ? NETWORK_ERROR_MESSAGE
                : COMMENTS_LOAD_ERROR_MESSAGE
              : null
          }
          onCommentsChange={setComments}
          onRetryLoadComments={() => {
            void loadTicket();
          }}
          onTicketNotFound={() => {
            setIsNotFound(true);
            setTicket(null);
            setComments([]);
          }}
        />
      )}
    </section>
  );
}
