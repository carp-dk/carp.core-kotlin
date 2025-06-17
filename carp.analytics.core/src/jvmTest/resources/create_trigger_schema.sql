-- Triggers table (manual and scheduled)
CREATE TABLE IF NOT EXISTS Triggers (
                                        id TEXT PRIMARY KEY,
                                        study_id TEXT NOT NULL,
                                        workflow_id TEXT NOT NULL,
                                        name TEXT NOT NULL,
                                        created_at TEXT NOT NULL,
                                        type TEXT NOT NULL, -- 'manual' or 'scheduled'
                                        cron_expr TEXT,     -- Only used for scheduled
                                        updated_at TEXT     -- Optional last update time
);

-- Index for listing by study and workflow
CREATE INDEX IF NOT EXISTS idx_triggers_study_workflow
    ON Triggers (study_id, workflow_id);

-- Trigger activations table
CREATE TABLE IF NOT EXISTS TriggerActivations (
                                                  id TEXT PRIMARY KEY,
                                                  trigger_id TEXT NOT NULL,
                                                  study_id TEXT NOT NULL,
                                                  fired_at TEXT NOT NULL,
                                                  workflow_exec_id TEXT
);

-- Index for querying activations by study
CREATE INDEX IF NOT EXISTS idx_triggeractivations_study
    ON TriggerActivations (study_id);

-- Index for querying activations by trigger
CREATE INDEX IF NOT EXISTS idx_triggeractivations_trigger
    ON TriggerActivations (trigger_id);
