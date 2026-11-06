CREATE SEQUENCE IF NOT EXISTS entity_id_seq START WITH 1;
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY CHECK (id > 0),
    name VARCHAR(255) NOT NULL UNIQUE CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    description CLOB NOT NULL,
    created_order BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE
);
CREATE TABLE IF NOT EXISTS notes (
    id BIGINT PRIMARY KEY CHECK (id > 0),
    name VARCHAR(255) NOT NULL CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    note_text CLOB NOT NULL,
    category_id BIGINT NOT NULL,
    priority INT NOT NULL CHECK (priority BETWEEN 1 AND 10),
    note_type VARCHAR(16) NOT NULL CHECK (note_type IN ('TEXT', 'CHECKLIST', 'SHOPPING')),
    source CLOB NOT NULL,
    store CLOB NOT NULL,
    created_order BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,
    CONSTRAINT fk_note_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);
CREATE TABLE IF NOT EXISTS note_items (
    note_id BIGINT NOT NULL,
    position INT NOT NULL CHECK (position >= 0),
    item_text CLOB NOT NULL CHECK (CHAR_LENGTH(TRIM(item_text)) > 0),
    completed BOOLEAN NOT NULL,
    PRIMARY KEY (note_id, position),
    CONSTRAINT fk_item_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_note_category ON notes(category_id);
CREATE INDEX IF NOT EXISTS idx_note_name_priority ON notes(name, priority);
