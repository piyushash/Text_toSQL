-- Insert only if data does not exist to avoid duplication on restart
INSERT INTO customers (name, city, email)
SELECT * FROM (VALUES
    ('Rahul Sharma', 'Delhi', 'rahul@gmail.com'),
    ('Amit Kumar', 'Mumbai', 'amit@gmail.com'),
    ('Priya Singh', 'Delhi', 'priya@gmail.com'),
    ('Neha Gupta', 'Patna', 'neha@gmail.com'),
    ('Rohit Verma', 'Bangalore', 'rohit@gmail.com'),
    ('Ankit Raj', 'Delhi', 'ankit@gmail.com'),
    ('Sneha Singh', 'Mumbai', 'sneha@gmail.com')
) AS v(name, city, email)
WHERE NOT EXISTS (SELECT 1 FROM customers LIMIT 1);

INSERT INTO orders (customer_id, amount, order_date)
SELECT * FROM (VALUES
    (1, 85000, '2026-08-01'::DATE),
    (1, 25000, '2026-08-10'::DATE),
    (2, 62000, '2026-08-03'::DATE),
    (2, 18000, '2026-08-15'::DATE),
    (3, 91000, '2026-08-05'::DATE),
    (3, 15000, '2026-08-20'::DATE),
    (4, 45000, '2026-08-07'::DATE),
    (5, 73000, '2026-08-09'::DATE),
    (6, 55000, '2026-08-12'::DATE),
    (7, 68000, '2026-08-14'::DATE)
) AS v(customer_id, amount, order_date)
WHERE NOT EXISTS (SELECT 1 FROM orders LIMIT 1);
