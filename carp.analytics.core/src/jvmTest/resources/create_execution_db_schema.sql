CREATE TABLE IF NOT EXISTS execution_state (
    execution_id TEXT PRIMARY KEY,
    study_id TEXT NOT NULL,
    workflow_id TEXT NOT NULL,
    status TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS execution_result (
        execution_id TEXT PRIMARY KEY,
        status TEXT NOT NULL,
        outputs_json TEXT NOT NULL,
        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (execution_id) REFERENCES execution_state (execution_id)
);

CREATE TABLE IF NOT EXISTS execution_artifact (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      execution_id TEXT NOT NULL,
      uri TEXT NOT NULL,
      name TEXT NOT NULL,
      type TEXT NOT NULL,
      mime_type TEXT,
      FOREIGN KEY (execution_id) REFERENCES execution_result (execution_id)
);
