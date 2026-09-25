import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createTicket } from '../api/tickets';
import {
  CreateTicketForm,
  type CreateTicketField,
} from '../components/CreateTicketForm';
import { ApiError } from '../types/api';
import type { CreateTicketRequest } from '../types/ticket';

const SERVER_ERROR_MESSAGE = 'Unable to create ticket. Please try again.';
const NETWORK_ERROR_MESSAGE =
  'Unable to connect to the server. Please check your connection and try again.';

function mapApiError(error: ApiError): {
  fieldErrors: Partial<Record<CreateTicketField, string>>;
  formError: string | null;
} {
  const fieldErrors: Partial<Record<CreateTicketField, string>> = {};

  if (error.fieldErrors) {
    for (const [field, message] of Object.entries(error.fieldErrors)) {
      if (field === 'title' || field === 'description' || field === 'priority' || field === 'assignee') {
        fieldErrors[field] = message;
      }
    }
  }

  if (error.message?.includes('Priority must be one of')) {
    fieldErrors.priority = error.message;
  }

  if (Object.keys(fieldErrors).length > 0) {
    return { fieldErrors, formError: null };
  }

  if (error.status === 400 && error.message) {
    return { fieldErrors, formError: error.message };
  }

  return { fieldErrors, formError: SERVER_ERROR_MESSAGE };
}

export function CreateTicketPage() {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<CreateTicketField, string>>>({});
  const [formError, setFormError] = useState<string | null>(null);

  const handleSubmit = async (request: CreateTicketRequest) => {
    setIsSubmitting(true);
    setFieldErrors({});
    setFormError(null);

    try {
      const ticket = await createTicket(request);

      if (ticket.id) {
        navigate(`/tickets/${ticket.id}`);
      } else {
        navigate('/tickets');
      }
    } catch (error) {
      if (error instanceof ApiError) {
        const mapped = mapApiError(error);
        setFieldErrors(mapped.fieldErrors);
        setFormError(mapped.formError);
      } else {
        setFormError(NETWORK_ERROR_MESSAGE);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="create-ticket-page">
      <div className="page-header">
        <h2>Create Ticket</h2>
        <Link to="/tickets" className="button-link">Back to tickets</Link>
      </div>
      <div className="card form-card">
        <CreateTicketForm
        isSubmitting={isSubmitting}
        fieldErrors={fieldErrors}
        formError={formError}
        onSubmit={(request) => {
          void handleSubmit(request);
        }}
        />
      </div>
    </section>
  );
}
