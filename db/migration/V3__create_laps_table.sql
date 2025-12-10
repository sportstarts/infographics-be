CREATE TABLE IF NOT EXISTS laps (
  id SERIAL PRIMARY KEY, 
  competition_id INT NOT NULL REFERENCES competitions(id),
  sport_kind_id INT NOT NULL REFERENCES sport_kinds(id),
  lats DOUBLE PRECISION[] NOT NULL,
  lons DOUBLE PRECISION[] NOT NULL,
  description TEXT NULL,
  sequence INT NOT NULL,
  lap_distance_meters DOUBLE PRECISION NOT NULL,
  points_distances_meters DOUBLE PRECISION[] NOT NULL
);
