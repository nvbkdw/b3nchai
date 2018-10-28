CREATE TABLE IF NOT EXISTS experiments (
id UUID PRIMARY KEY,
create_time timestamp,
last_update_time timestamp,
account_id VARCHAR(256),
type VARCHAR(256));