import { FormEvent, useState } from 'react';
import { formatDateTime } from '../lib/formatDateTime';
import {
  TICKET_PRIORITY_OPTIONS,
  type Ticket,
  type TicketPriority,
  type UpdateTicketRequest,
} from '../types/ticket';

export type TicketEditField = keyof UpdateTicketRequest;

export interface TicketEditFormValues {
  title: string;
  description: string;
  priority: TicketPriority | '';
  assignee: string;
}

interface TicketEditFormProps {
  ticket: Ticket;
  isSaving: boolean;
  fieldErrors: Partial<Record<TicketEditField, string>>;
  formError: string | null;
  onSave: (request: UpdateTicketRequest) => void;
  onCancel: () => void;
}

function ticketToFormValues(ticket: Ticket): TicketEditFormValues {
  return {
    title: ticket.title,
    description: ticket.description,
    priority: ticket.priority,
    assignee: ticket.assignee,
  };
}

function validateClient(values: TicketEditFormValues): Partial<Record<TicketEditField, string>> {
  const errors: Partial<Record<TicketEditField, string>> = {};

  if (!values.title.trim()) {
    errors.title = 'Title is required.';
  }

  if (!values.description.trim()) {
    errors.description = 'Description is required.';
  }

  if (!values.priority) {
    errors.priority = 'Priority is required.';
  }

  if (!values.assignee.trim()) {
    errors.assignee = 'Assignee is required.';
  }

  return errors;
}

export function TicketEditForm({
  ticket,
  isSaving,
  fieldErrors,
  formError,
  onSave,
  onCancel,
}: TicketEditFormProps) {
  const [values, setValues] = useState<TicketEditFormValues>(() => ticketToFormValues(ticket));
  const [clientErrors, setClientErrors] = useState<Partial<Record<TicketEditField, string>>>({});

  const displayedErrors: Partial<Record<TicketEditField, string>> = {
    ...clientErrors,
    ...fieldErrors,
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const nextClientErrors = validateClient(values);
    setClientErrors(nextClientErrors);

    if (Object.keys(nextClientErrors).length > 0) {
      return;
    }

    onSave({
      title: values.title.trim(),
      description: values.description.trim(),
      priority: values.priority as TicketPriority,
      assignee: values.assignee.trim(),
    });
  };

  const fieldErrorId = (field: TicketEditField) => `edit-${field}-error`;

  return (
    <form className="ticket-edit-form" onSubmit={handleSubmit} noValidate>
      {formError && (
        <div className="ticket-edit-form-error" role="alert">
          <p>{formError}</p>
        </div>
      )}

      <dl className="ticket-details-meta">
        <div className="ticket-details-meta-item">
          <dt>Ticket ID</dt>
          <dd>{ticket.id}</dd>
        </div>
        <div className="ticket-details-meta-item">
          <dt>Status</dt>
          <dd>{ticket.status}</dd>
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

      <div className="form-field">
        <label htmlFor="edit-ticket-title">
          Title <span className="required-indicator">(required)</span>
        </label>
        <input
          id="edit-ticket-title"
          type="text"
          value={values.title}
          required
          aria-invalid={displayedErrors.title ? true : undefined}
          aria-describedby={displayedErrors.title ? fieldErrorId('title') : undefined}
          disabled={isSaving}
          onChange={(event) => setValues((current) => ({ ...current, title: event.target.value }))}
        />
        {displayedErrors.title && (
          <p id={fieldErrorId('title')} className="field-error" role="alert">
            {displayedErrors.title}
          </p>
        )}
      </div>

      <div className="form-field">
        <label htmlFor="edit-ticket-description">
          Description <span className="required-indicator">(required)</span>
        </label>
        <textarea
          id="edit-ticket-description"
          value={values.description}
          required
          rows={4}
          aria-invalid={displayedErrors.description ? true : undefined}
          aria-describedby={displayedErrors.description ? fieldErrorId('description') : undefined}
          disabled={isSaving}
          onChange={(event) =>
            setValues((current) => ({ ...current, description: event.target.value }))
          }
        />
        {displayedErrors.description && (
          <p id={fieldErrorId('description')} className="field-error" role="alert">
            {displayedErrors.description}
          </p>
        )}
      </div>

      <div className="form-field">
        <label htmlFor="edit-ticket-priority">
          Priority <span className="required-indicator">(required)</span>
        </label>
        <select
          id="edit-ticket-priority"
          value={values.priority}
          required
          aria-invalid={displayedErrors.priority ? true : undefined}
          aria-describedby={displayedErrors.priority ? fieldErrorId('priority') : undefined}
          disabled={isSaving}
          onChange={(event) =>
            setValues((current) => ({
              ...current,
              priority: event.target.value as TicketPriority | '',
            }))
          }
        >
          <option value="">Select priority</option>
          {TICKET_PRIORITY_OPTIONS.map((priority) => (
            <option key={priority} value={priority}>
              {priority}
            </option>
          ))}
        </select>
        {displayedErrors.priority && (
          <p id={fieldErrorId('priority')} className="field-error" role="alert">
            {displayedErrors.priority}
          </p>
        )}
      </div>

      <div className="form-field">
        <label htmlFor="edit-ticket-assignee">
          Assignee <span className="required-indicator">(required)</span>
        </label>
        <input
          id="edit-ticket-assignee"
          type="text"
          value={values.assignee}
          required
          aria-invalid={displayedErrors.assignee ? true : undefined}
          aria-describedby={displayedErrors.assignee ? fieldErrorId('assignee') : undefined}
          disabled={isSaving}
          onChange={(event) => setValues((current) => ({ ...current, assignee: event.target.value }))}
        />
        {displayedErrors.assignee && (
          <p id={fieldErrorId('assignee')} className="field-error" role="alert">
            {displayedErrors.assignee}
          </p>
        )}
      </div>

      <div className="form-actions">
        <button type="button" className="button-secondary" disabled={isSaving} onClick={onCancel}>
          Cancel
        </button>
        <button type="submit" disabled={isSaving}>
          {isSaving ? 'Saving...' : 'Save'}
        </button>
      </div>
    </form>
  );
}
