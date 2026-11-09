
-- Sample categories
INSERT INTO category (name)
VALUES ('Work'), ('Personal'), ('Study')
ON CONFLICT (name) DO NOTHING;

-- Sample tags
INSERT INTO tag (name)
VALUES ('Important'), ('Urgent'), ('Ideas')
ON CONFLICT (name) DO NOTHING;
