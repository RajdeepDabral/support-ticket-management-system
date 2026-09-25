import { FormEvent, useState } from 'react';
import type { CreateCommentRequest } from '../types/ticket';

interface AddCommentFormProps {
  author: string;
  isSubmitting: boolean;
  contentError: string | null;
  formError: string | null;
  onSubmit: (request: CreateCommentRequest) => void;
}

export const EMPTY_COMMENT_ERROR = 'Comment cannot be empty.';

function validateContent(content: string): string | null {
  if (!content.trim()) {
    return EMPTY_COMMENT_ERROR;
  }

  return null;
}

export function AddCommentForm({
  author,
  isSubmitting,
  contentError,
  formError,
  onSubmit,
}: AddCommentFormProps) {
  const [content, setContent] = useState('');
  const [clientError, setClientError] = useState<string | null>(null);

  const displayedContentError = clientError ?? contentError;

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const nextClientError = validateContent(content);
    setClientError(nextClientError);

    if (nextClientError) {
      return;
    }

    onSubmit({
      content: content.trim(),
      author: author.trim(),
    });
  };

  return (
    <form className="add-comment-form" onSubmit={handleSubmit} noValidate>
      {formError && (
        <div className="add-comment-form-error" role="alert">
          <p>{formError}</p>
        </div>
      )}

      <div className="form-field">
        <label htmlFor="comment-content">Comment</label>
        <textarea
          id="comment-content"
          value={content}
          rows={4}
          required
          aria-invalid={displayedContentError ? true : undefined}
          aria-describedby={displayedContentError ? 'comment-content-error' : undefined}
          disabled={isSubmitting}
          onChange={(event) => setContent(event.target.value)}
        />
        {displayedContentError && (
          <p id="comment-content-error" className="field-error" role="alert">
            {displayedContentError}
          </p>
        )}
      </div>

      <div className="form-actions">
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Adding comment...' : 'Add Comment'}
        </button>
      </div>
    </form>
  );
}
