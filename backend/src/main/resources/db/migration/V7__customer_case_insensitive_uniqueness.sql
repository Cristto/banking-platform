CREATE UNIQUE INDEX customers_document_number_normalized_key
    ON customers (UPPER(BTRIM(document_number)));

CREATE UNIQUE INDEX customers_email_normalized_key
    ON customers (LOWER(BTRIM(email)));
