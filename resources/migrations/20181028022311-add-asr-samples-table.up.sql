CREATE TABLE IF NOT EXISTS asr_samples (
id UUID PRIMARY KEY,
experiment_id UUID references experiments(id),
name VARCHAR(256),
audio_format VARCHAR(256),
audio_duration_milli_sec integer,
audio_sample_rate integer,
audio_encoding VARCHAR(256),
audio_channel integer,
transcript_s3_location TEXT);