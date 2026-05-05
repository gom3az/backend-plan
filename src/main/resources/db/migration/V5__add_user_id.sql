CREATE INDEX IF NOT EXISTS idx_tasks_user_id ON tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_due_date ON tasks(due_date);
CREATE INDEX IF NOT EXISTS idx_after_id ON tasks(created_at);
