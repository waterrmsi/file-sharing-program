CREATE TABLE files (
    file_id UUID PRIMARY KEY,
    owner_user_id varchar(36),
    filename text NOT NULL,
    bucket varchar(64) NOT NULL,
    object_key text NOT NULL,
    content_type text NOT NULL,
    size bigint NOT NULL,
    created_at timestamptz NOT NULL,
    is_public boolean NOT NULL DEFAULT true
);

CREATE TABLE file_activity_log (
    id serial PRIMARY KEY,
    file_id uuid,
    user_id varchar(36),
    file_operation varchar(8) NOT NULL,
    occurred_at timestamptz NOT NULL,
    CONSTRAINT fk_file
        FOREIGN KEY (file_id)
        REFERENCES files(file_id)
        ON DELETE SET NULL
);