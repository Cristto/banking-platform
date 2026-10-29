-- Existing authorized purchases awaiting settlement are pending confirmation.
UPDATE purchases SET status = 'PENDING' WHERE status = 'AUTHORIZED';
