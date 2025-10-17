INSERT INTO rhub_customers (id, first_name, last_name, email, phone)
VALUES
    (1, 'Anna', 'Karlsson', 'anna.karlsson@example.com', '0701111111'),
    (2, 'Johan', 'Nilsson', 'johan.nilsson@example.com', '0702222222'),
    (3, 'Sara', 'Persson', 'sara.persson@example.com', '0703333333'),
    (4, 'Mikael', 'Berg', 'mikael.berg@example.com', '0704444444');

INSERT INTO rhub_assets (id, asset_name, category, daily_rate, available)
VALUES
    (1, 'Borrmaskin', 'Verktyg', 150.0, true),
    (2, 'Släpvagn', 'Fordon', 400.0, true),
    (3, 'Projektor', 'Elektronik', 250.0, true);

-- Exempelbokningar
INSERT INTO rhub_bookings (id, asset_id, customer_id, start_date, end_date, active, note)
VALUES
    (1, 1, 1, '2025-10-01', NULL, true, 'För renovering');
