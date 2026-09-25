import { FormEvent, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  TICKET_PRIORITY_OPTIONS,
  type CreateTicketRequest,
  type TicketPriority,
} from '../types/ticket';

export type CreateTicketField = keyof CreateTicketRequest;

export interface CreateTicketFormValues {
  title: string;
  description: string;
  priority: TicketPriority | '';
  assignee: string;
}

interface CreateTicketFormProps {
  isSubmitting: boolean;
  fieldErrors: Partial<Record<CreateTicketField, string>>;
  formError: string | null;
  onSubmit: (request: CreateTicketRequest) => void;
}

const INITIAL_VALUES: CreateTicketFormValues = {
  title: '',
  description: '',
  priority: '',
  assignee: '',
};

function validateClient(values: CreateTicketFormValues): Partial<Record<CreateTicketField, string>> {
  const errors: Partial<Record<CreateTicketField, string>> = {};

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

export function CreateTicketForm({
  isSubmitting,
  fieldErrors,
  formError,
  onSubmit,
}: CreateTicketFormProps) {
  const [values, setValues] = useState<CreateTicketFormValues>(INITIAL_VALUES);
  const [clientErrors, setClientErrors] = useState<Partial<Record<CreateTicketField, string>>>({});

  const displayedErrors: Partial<Record<CreateTicketField, string>> = {
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

    onSubmit({
      title: values.title.trim(),
      description: values.description.trim(),
      priority: values.priority as TicketPriority,
      assignee: values.assignee.trim(),
    });
  };

  const fieldErrorId = (field: CreateTicketField) => `${field}-error`;

  return (
    <form className="create-ticket-form" onSubmit={handleSubmit} noValidate>
      {formError && (
        <div className="create-ticket-form-error" role="alert">
          <p>{formError}</p>
        </div>
      )}

      <div className="form-field">
        <label htmlFor="ticket-title">
          Title <span className="required-indicator">(required)</span>
        </label>
        <input
          id="ticket-title"
          type="text"
          value={values.title}
          required
          aria-invalid={displayedErrors.title ? true : undefined}
          aria-describedby={displayedErrors.title ? fieldErrorId('title') : undefined}
          disabled={isSubmitting}
          onChange={(event) => setValues((current) => ({ ...current, title: event.target.value }))}
        />
        {displayedErrors.title && (
          <p id={fieldErrorId('title')} className="field-error" role="alert">
            {displayedErrors.title}
          </p>
        )}
      </div>

      <div className="form-field">
        <label htmlFor="ticket-description">
          Description <span className="required-indicator">(required)</span>
        </label>
        <textarea
          id="ticket-description"
          value={values.description}
          required
          rows={4}
          aria-invalid={displayedErrors.description ? true : undefined}
          aria-describedby={displayedErrors.description ? fieldErrorId('description') : undefined}
          disabled={isSubmitting}
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
        <label htmlFor="ticket-priority">
          Priority <span className="required-indicator">(required)</span>
        </label>
        <select
          id="ticket-priority"
          value={values.priority}
          required
          aria-invalid={displayedErrors.priority ? true : undefined}
          aria-describedby={displayedErrors.priority ? fieldErrorId('priority') : undefined}
          disabled={isSubmitting}
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
        <label htmlFor="ticket-assignee">
          Assignee <span className="required-indicator">(required)</span>
        </label>
        <input
          id="ticket-assignee"
          type="text"
          value={values.assignee}
          required
          aria-invalid={displayedErrors.assignee ? true : undefined}
          aria-describedby={displayedErrors.assignee ? fieldErrorId('assignee') : undefined}
          disabled={isSubmitting}
          onChange={(event) => setValues((current) => ({ ...current, assignee: event.target.value }))}
        />
        {displayedErrors.assignee && (
          <p id={fieldErrorId('assignee')} className="field-error" role="alert">
            {displayedErrors.assignee}
          </p>
        )}
      </div>

      <div className="form-actions">
        <Link to="/tickets" className="button-link">
          Cancel
        </Link>
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Creating...' : 'Create Ticket'}
        </button>
      </div>
    </form>
  );
}
