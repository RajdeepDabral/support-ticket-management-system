import { formatDateTime } from '../lib/formatDateTime';
import type { Comment } from '../types/ticket';

interface CommentItemProps {
  comment: Comment;
}

export function CommentItem({ comment }: CommentItemProps) {
  return (
    <article className="comment-item">
      {comment.author && <p className="comment-author">{comment.author}</p>}
      <p className="comment-content">{comment.content}</p>
      {comment.createdAt && (
        <p className="comment-timestamp">
          <time dateTime={comment.createdAt}>{formatDateTime(comment.createdAt)}</time>
        </p>
      )}
    </article>
  );
}
