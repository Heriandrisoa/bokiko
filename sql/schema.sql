-- Safe to run several times (IF NOT EXISTS everywhere)

CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(30)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(60)  NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS users_username_uq ON users (lower(username));
CREATE UNIQUE INDEX IF NOT EXISTS users_email_uq    ON users (lower(email));

CREATE TABLE IF NOT EXISTS books (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title             VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_path         VARCHAR(500) NOT NULL,   -- relative to books.dir, e.g. 12/3f2a...pdf
    cover_path        VARCHAR(500),            -- relative to books.dir, NULL if no cover
    page_count        INT          NOT NULL,
    file_size         BIGINT       NOT NULL,
    sha256            CHAR(64)     NOT NULL,
    last_page         INT          NOT NULL DEFAULT 1,
    last_read_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (user_id, sha256)                   -- blocks duplicate imports
);
CREATE INDEX IF NOT EXISTS books_user_idx ON books (user_id);

CREATE TABLE IF NOT EXISTS book_marks (
    id         BIGSERIAL PRIMARY KEY,
    book_id    BIGINT      NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    user_id    BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    page       INT         NOT NULL CHECK (page > 0),
    note       VARCHAR(500) NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS book_marks_book_user_idx ON book_marks (book_id, user_id, created_at DESC);
