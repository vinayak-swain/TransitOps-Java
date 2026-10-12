-- ═══════════════════════════════════════════════════════════════════════════════
-- TransitOps — Database Schema (equivalent to migrate.js + oauthMigrate.js)
-- Run: This is executed automatically by Hibernate ddl-auto=update via JPA entities
-- This file is kept as reference / manual migration fallback.
-- ═══════════════════════════════════════════════════════════════════════════════

-- Users table (with OAuth2 columns included)
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    role VARCHAR(50) NOT NULL CHECK (role IN ('fleet_manager', 'dispatcher', 'safety_officer', 'financial_analyst')),
    is_active BOOLEAN DEFAULT TRUE,
    provider VARCHAR(20) DEFAULT 'local' NOT NULL,
    google_id VARCHAR(100) UNIQUE,
    profile_picture VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Vehicles table
CREATE TABLE IF NOT EXISTS vehicles (
    id SERIAL PRIMARY KEY,
    registration_number VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    model VARCHAR(100),
    type VARCHAR(50) NOT NULL CHECK (type IN ('Truck', 'Van', 'Car', 'Motorcycle', 'Bus', 'Trailer')),
    max_load_kg DECIMAL(10,2) NOT NULL CHECK (max_load_kg > 0),
    odometer_km DECIMAL(10,2) DEFAULT 0,
    acquisition_cost DECIMAL(12,2) NOT NULL CHECK (acquisition_cost >= 0),
    status VARCHAR(20) DEFAULT 'Available' CHECK (status IN ('Available', 'On Trip', 'In Shop', 'Retired')),
    region VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Drivers table
CREATE TABLE IF NOT EXISTS drivers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) UNIQUE NOT NULL,
    license_category VARCHAR(10) NOT NULL,
    license_expiry DATE NOT NULL,
    contact VARCHAR(20),
    safety_score INTEGER DEFAULT 100 CHECK (safety_score >= 0 AND safety_score <= 100),
    status VARCHAR(20) DEFAULT 'Available' CHECK (status IN ('Available', 'On Trip', 'Off Duty', 'Suspended')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Trips table
CREATE TABLE IF NOT EXISTS trips (
    id SERIAL PRIMARY KEY,
    source VARCHAR(200) NOT NULL,
    destination VARCHAR(200) NOT NULL,
    vehicle_id INTEGER REFERENCES vehicles(id) ON DELETE RESTRICT,
    driver_id INTEGER REFERENCES drivers(id) ON DELETE RESTRICT,
    cargo_weight_kg DECIMAL(10,2) NOT NULL CHECK (cargo_weight_kg >= 0),
    planned_distance_km DECIMAL(10,2) NOT NULL CHECK (planned_distance_km > 0),
    actual_distance_km DECIMAL(10,2),
    fuel_consumed_l DECIMAL(10,2),
    revenue DECIMAL(12,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'Draft' CHECK (status IN ('Draft', 'Dispatched', 'Completed', 'Cancelled')),
    notes TEXT,
    dispatched_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_by INTEGER REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Maintenance logs table
CREATE TABLE IF NOT EXISTS maintenance_logs (
    id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(id) ON DELETE CASCADE,
    type VARCHAR(100) NOT NULL,
    description TEXT,
    cost DECIMAL(12,2) DEFAULT 0 CHECK (cost >= 0),
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'Active' CHECK (status IN ('Active', 'Closed')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Fuel logs table
CREATE TABLE IF NOT EXISTS fuel_logs (
    id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(id) ON DELETE CASCADE,
    trip_id INTEGER REFERENCES trips(id) ON DELETE SET NULL,
    liters DECIMAL(10,2) NOT NULL CHECK (liters > 0),
    cost_per_liter DECIMAL(10,4) NOT NULL CHECK (cost_per_liter > 0),
    total_cost DECIMAL(12,2) GENERATED ALWAYS AS (liters * cost_per_liter) STORED,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    odometer_reading DECIMAL(10,2),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Expenses table
CREATE TABLE IF NOT EXISTS expenses (
    id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(id) ON DELETE CASCADE,
    trip_id INTEGER REFERENCES trips(id) ON DELETE SET NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN ('Toll', 'Maintenance', 'Fuel', 'Insurance', 'Other')),
    amount DECIMAL(12,2) NOT NULL CHECK (amount >= 0),
    description TEXT,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_trips_vehicle_id ON trips(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_trips_driver_id ON trips(driver_id);
CREATE INDEX IF NOT EXISTS idx_trips_status ON trips(status);
CREATE INDEX IF NOT EXISTS idx_maintenance_vehicle_id ON maintenance_logs(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_fuel_vehicle_id ON fuel_logs(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_expenses_vehicle_id ON expenses(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_users_google_id ON users(google_id);
CREATE INDEX IF NOT EXISTS idx_users_provider ON users(provider);
