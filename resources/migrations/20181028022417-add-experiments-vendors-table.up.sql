CREATE TABLE IF NOT EXISTS experiments_vendors(
id UUID PRIMARY KEY,
experiment_id UUID references experiments(id),
vendor_id UUID references vendors(id));