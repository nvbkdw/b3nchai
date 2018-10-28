CREATE TABLE IF NOT EXISTS asr_tasks (
id UUID PRIMARY KEY,
asr_sample_id UUID references asr_samples(id),
vendor UUID references vendors(id),
vendor_api_params JSONB,
result_s3_location TEXT,
wer real,
insertion real,
deletion real,
substitution real);