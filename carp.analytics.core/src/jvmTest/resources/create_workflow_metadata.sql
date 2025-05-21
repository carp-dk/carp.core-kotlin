CREATE TABLE workflow_metadata (
                                   id TEXT PRIMARY KEY,
                                   study_id TEXT NOT NULL,
                                   name TEXT NOT NULL,
                                   description TEXT,
                                   version_major INTEGER NOT NULL,
                                   version_minor INTEGER,
                                   file_path TEXT NOT NULL,
                                   created_at TEXT NOT NULL,
                                   updated_at TEXT NOT NULL
);
