import { useState } from 'react';
import { addComment } from '../api/tickets';
import { AddCommentForm } from './AddCommentForm';
import { CommentItem } from './CommentItem';
import { ApiError } from '../types/api';
import type { Comment, CreateCommentRequest } from '../types/ticket';

const COMMENTS_LOAD_ERROR_MESSAGE = 'Unable to load comments. Please try again.';
const ADD_COMMENT_ERROR_MESSAGE = 'Unable to add comment. Please try again.';
const NETWORK_ERROR_MESSAGE =
  'Unable to connect to the server. Please check your connection and try again.';

interface TicketCommentsSectionProps {
  ticketId: number;
  author: string;
  comments: Comment[];
  isLoadingComments: boolean;
  commentsLoadError: string | null;
  onCommentsChange: (comments: Comment[]) => void;
  onRetryLoadComments: () => void;
  onTicketNotFound: () => void;
}

function mapAddCommentApiError(error: ApiError): {
  contentError: string | null;
  formError: string | null;
} {
  if (error.fieldErrors?.content) {
    return { contentError: error.fieldErrors.content, formError: null };
  }

  if (error.fieldErrors?.author) {
    return { contentError: null, formError: error.fieldErrors.author };
  }

  if (error.status === 400 && error.message) {
    return { contentError: null, formError: error.message };
  }

  return { contentError: null, formError: ADD_COMMENT_ERROR_MESSAGE };
}

export function TicketCommentsSection({
  ticketId,
  author,
  comments,
  isLoadingComments,
  commentsLoadError,
  onCommentsChange,
  onRetryLoadComments,
  onTicketNotFound,
}: TicketCommentsSectionProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [contentError, setContentError] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [formResetKey, setFormResetKey] = useState(0);

  const handleSubmit = async (request: CreateCommentRequest) => {
    if (isSubmitting) {
      return;
    }

    setIsSubmitting(true);
    setContentError(null);
    setFormError(null);

    try {
      const createdComment = await addComment(ticketId, request);
      onCommentsChange([...comments, createdComment]);
      setFormResetKey((current) => current + 1);
    } catch (error) {
      if (error instanceof ApiError) {
        if (error.status === 404) {
          onTicketNotFound();
        } else {
          const mapped = mapAddCommentApiError(error);
          setContentError(mapped.contentError);
          setFormError(mapped.formError);
        }
      } else {
        setFormError(NETWORK_ERROR_MESSAGE);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="ticket-comments-section" aria-labelledby="ticket-comments-heading">
      <h3 id="ticket-comments-heading">Comments</h3>

      {isLoadingComments && (
        <p className="ticket-comments-loading" role="status">Loading comments...</p>
      )}

      {!isLoadingComments && commentsLoadError && (
        <div className="ticket-comments-error" role="alert">
          <p>{commentsLoadError}</p>
          <button type="button" onClick={onRetryLoadComments}>Try again</button>
        </div>
      )}

      {!isLoadingComments && !commentsLoadError && comments.length === 0 && (
        <p className="ticket-comments-empty" role="status">No comments yet.</p>
      )}

      {!isLoadingComments && !commentsLoadError && comments.length > 0 && (
        <ul className="comment-list">
          {comments.map((comment) => (
            <li key={comment.id}>
              <CommentItem comment={comment} />
            </li>
          ))}
        </ul>
      )}

      {!isLoadingComments && !commentsLoadError && (
        <AddCommentForm
          key={formResetKey}
          author={author}
          isSubmitting={isSubmitting}
          contentError={contentError}
          formError={formError}
          onSubmit={(request) => {
            void handleSubmit(request);
          }}
        />
      )}
    </section>
  );
}

export { COMMENTS_LOAD_ERROR_MESSAGE };
