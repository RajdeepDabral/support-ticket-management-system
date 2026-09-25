CREATE TABLE ticket (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    priority    VARCHAR(30) NOT NULL,
    status      VARCHAR(30) NOT NULL,
    assignee    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_ticket_status ON ticket (status);

CREATE TABLE comment (
    id          BIGSERIAL PRIMARY KEY,
    ticket_id   BIGINT NOT NULL,
    content     TEXT NOT NULL,
    author      VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id)
);

CREATE INDEX idx_comment_ticket_id ON comment (ticket_id);
