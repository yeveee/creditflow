INSERT INTO utilisateur (username, password, role) VALUES
('client1', '$2a$10$btvqBBev8soN5WKKCOoMqultikqtmaI3PVlyN.NeVC39jiCrCr.hS', 'CLIENT'),
('client2', '$2a$10$btvqBBev8soN5WKKCOoMqultikqtmaI3PVlyN.NeVC39jiCrCr.hS', 'CLIENT'),
('analyste1', '$2a$10$btvqBBev8soN5WKKCOoMqultikqtmaI3PVlyN.NeVC39jiCrCr.hS', 'ANALYSTE'),
('directeur1', '$2a$10$btvqBBev8soN5WKKCOoMqultikqtmaI3PVlyN.NeVC39jiCrCr.hS', 'DIRECTEUR')
ON CONFLICT (username) DO NOTHING;