-- Создание таблицы routes
CREATE TABLE routes (
    route_id SERIAL PRIMARY KEY,
    departure_city VARCHAR(50),
    destination_city VARCHAR(50),
    distance_km DECIMAL(10,2)
);

-- Создание таблицы buses
CREATE TABLE buses (
    bus_id SERIAL PRIMARY KEY,
    model VARCHAR(50),
    capacity INT
);

-- Создание таблицы trips
CREATE TABLE trips (
    trip_id SERIAL PRIMARY KEY,
    route_id INT REFERENCES routes(route_id),
    bus_id INT REFERENCES buses(bus_id),
    departure_time TIMESTAMP,
    arrival_time TIMESTAMP,
    ticket_price DECIMAL(10,2)
);

-- Создание таблицы passengers
CREATE TABLE passengers (
    passenger_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(20)
);

-- Создание таблицы tickets
CREATE TABLE tickets (
    ticket_id SERIAL PRIMARY KEY,
    trip_id INT REFERENCES trips(trip_id),
    passenger_id INT REFERENCES passengers(passenger_id),
    seat_number INT,
    purchase_date TIMESTAMP
);