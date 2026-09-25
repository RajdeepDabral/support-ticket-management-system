import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { addComment } from '../api/tickets';
import { ApiError } from '../types/api';
import type { Comment } from '../types/ticket';
import { EMPTY_COMMENT_ERROR } from './AddCommentForm';
import { TicketCommentsSection } from './TicketCommentsSection';

vi.mock('../api/tickets');

const mockAddComment = vi.mocked(addComment);

const existingComment: Comment = {
  id: 1,
  ticketId: 42,
  content: 'Investigating the issue.',
  author: 'jane.doe',
  createdAt: '2026-09-25T09:00:00Z',
};

const defaultProps = {
  ticketId: 42,
  author: 'john.doe',
  comments: [] as Comment[],
  isLoadingComments: false,
  commentsLoadError: null as string | null,
  onCommentsChange: vi.fn(),
  onRetryLoadComments: vi.fn(),
  onTicketNotFound: vi.fn(),
};

function renderCommentsSection(overrides: Partial<typeof defaultProps> = {}) {
  const props = { ...defaultProps, ...overrides };
  return render(<TicketCommentsSection {...props} />);
}

describe('TicketCommentsSection', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the comments section', () => {
    renderCommentsSection();

    expect(screen.getByRole('heading', { name: 'Comments' })).toBeInTheDocument();
  });

  it('displays existing comments with author and timestamp', () => {
    renderCommentsSection({ comments: [existingComment] });

    expect(screen.getByText('Investigating the issue.')).toBeInTheDocument();
    expect(screen.getByText('jane.doe')).toBeInTheDocument();
    expect(screen.getByRole('time')).toBeInTheDocument();
  });

  it('displays a loading state while comments are being retrieved', () => {
    renderCommentsSection({ isLoadingComments: true });

    expect(screen.getByText('Loading comments...')).toBeInTheDocument();
    expect(screen.queryByText('No comments yet.')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Comment')).not.toBeInTheDocument();
  });

  it('displays an empty state after a successful load with no comments', () => {
    renderCommentsSection({ comments: [] });

    expect(screen.getByText('No comments yet.')).toBeInTheDocument();
  });

  it('displays an API error and allows retry', () => {
    const onRetryLoadComments = vi.fn();

    renderCommentsSection({
      commentsLoadError: 'Unable to load comments. Please try again.',
      onRetryLoadComments,
    });

    expect(screen.getByText('Unable to load comments. Please try again.')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Try again' }));
    expect(onRetryLoadComments).toHaveBeenCalledTimes(1);
  });

  it('displays the comment input', () => {
    renderCommentsSection();

    expect(screen.getByLabelText('Comment')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Add Comment' })).toBeInTheDocument();
  });

  it('rejects an empty comment before submitting', async () => {
    renderCommentsSection();

    fireEvent.click(screen.getByRole('button', { name: 'Add Comment' }));

    expect(await screen.findByText(EMPTY_COMMENT_ERROR)).toBeInTheDocument();
    expect(mockAddComment).not.toHaveBeenCalled();
  });

  it('rejects a whitespace-only comment before submitting', async () => {
    renderCommentsSection();

    fireEvent.change(screen.getByLabelText('Comment'), { target: { value: '   ' } });
    fireEvent.click(screen.getByRole('button', { name: 'Add Comment' }));

    expect(await screen.findByText(EMPTY_COMMENT_ERROR)).toBeInTheDocument();
    expect(mockAddComment).not.toHaveBeenCalled();
  });

  it('submits a valid comment with the correct API request', async () => {
    const onCommentsChange = vi.fn();
    mockAddComment.mockResolvedValue({
      id: 2,
      ticketId: 42,
      content: 'I have investigated the issue.',
      author: 'john.doe',
      createdAt: '2026-09-25T11:30:00Z',
    });

    renderCommentsSection({ onCommentsChange });

    fireEvent.change(screen.getByLabelText('Comment'), {
      target: { value: 'I have investigated the issue.' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Add Comment' }));

    await waitFor(() => {
      expect(mockAddComment).toHaveBeenCalledWith(42, {
        content: 'I have investigated the issue.',
        author: 'john.doe',
      });
    });

    const payload = mockAddComment.mock.calls[0][1];
    expect(payload).not.toHaveProperty('status');
    expect(payload).not.toHaveProperty('priority');
    expect(payload).not.toHaveProperty('title');
  });

  it('prevents duplicate submissions while adding a comment', async () => {
    let resolveAdd: (value: Comment) => void = () => {};
    mockAddComment.mockImplementation(
      () =>
        new Promise<Comment>((resolve) => {
          resolveAdd = resolve;
        }),
    );

    renderCommentsSection();

    fireEvent.change(screen.getByLabelText('Comment'), {
      target: { value: 'I have investigated the issue.' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Add Comment' }));

    expect(screen.getByRole('button', { name: 'Adding comment...' })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: 'Adding comment...' }));
    expect(mockAddComment).toHaveBeenCalledTimes(1);

    resolveAdd({
      id: 2,
      ticketId: 42,
      content: 'I have investigated the issue.',
      author: 'john.doe',
      createdAt: '2026-09-25T11:30:00Z',
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Add Comment' })).toBeInTheDocument();
    });
  });

  it('displays a new comment after successful creation', async () => {
    const onCommentsChange = vi.fn();
    mockAddComment.mockResolvedValue({
      id: 2,
      ticketId: 42,
      content: 'I have investigated the issue.',
      author: 'john.doe',
      createdAt: '2026-09-25T11:30:00Z',
    });

    renderCommentsSection({ onCommentsChange });

    fireEvent.change(screen.getByLabelText('Comment'), {
      target: { value: 'I have investigated the issue.' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Add Comment' }));

    await waitFor(() => {
      expect(onCommentsChange).toHaveBeenCalledWith([
        {
          id: 2,
          ticketId: 42,
          content: 'I have investigated the issue.',
          author: 'john.doe',
          createdAt: '2026-09-25T11:30:00Z',
        },
      ]);
    });
  });

  it('clears the input only after successful creation', async () => {
    mockAddComment.mockResolvedValue({
      id: 2,
      ticketId: 42,
      content: 'I have investigated the issue.',
      author: 'john.doe',
      createdAt: '2026-09-25T11:30:00Z',
    });

    renderCommentsSection();

    const textarea = screen.getByLabelText('Comment') as HTMLTextAreaElement;
    fireEvent.change(textarea, { target: { value: 'I have investigated the issue.' } });
    fireEvent.click(screen.getByRole('button', { name: 'Add Comment' }));

    await waitFor(() => {
      expect((screen.getByLabelText('Comment') as HTMLTextAreaElement).value).toBe('');
    });
  });

  it('preserves the entered comment after a failed submission', async () => {
    mockAddComment.mockRejectedValue(
      new ApiError(500, {
        status: 500,
        code: 'INTERNAL_SERVER_ERROR',
        message: 'An unexpected error occurred.',
      }),
    );

    renderCommentsSection();

    fireEvent.change(screen.getByLabelText('Comment'), {
      target: { value: 'I have investigated the issue.' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Add Comment' }));

    expect(
      await screen.findByText('Unable to add comment. Please try again.'),
    ).toBeInTheDocument();
    expect((screen.getByLabelText('Comment') as HTMLTextAreaElement).value).toBe(
      'I have investigated the issue.',
    );
  });

  it('displays backend validation errors for HTTP 400', async () => {
    mockAddComment.mockRejectedValue(
      new ApiError(400, {
        status: 400,
        code: 'VALIDATION_ERROR',
        message: 'Request validation failed.',
        fieldErrors: {
          content: 'Content must not be blank',
        },
      }),
    );

    renderCommentsSection();

    fireEvent.change(screen.getByLabelText('Comment'), {
      target: { value: 'Valid looking content' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Add Comment' }));

    expect(await screen.findByText('Content must not be blank')).toBeInTheDocument();
  });
});
