-- Database Schema for EPDA (Enterprise Programming - Dynamic Appointment System)
-- Target Database: Apache Derby
-- Run this FULL script to drop and recreate all tables from scratch.

-- create database EPDA under JAVA before running sql

CREATE TABLE Manager (
    manager_id VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Technician (
    technician_id VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    specialty VARCHAR(100) NOT NULL,
    available BOOLEAN DEFAULT TRUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Customer (
    customer_id VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Service (
    service_id VARCHAR(30) PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Appointment (
    appointment_id VARCHAR(30) PRIMARY KEY,
    customer_id VARCHAR(30) NOT NULL,
    technician_id VARCHAR(30) NOT NULL,
    service_id VARCHAR(30) NOT NULL,
    appointment_datetime TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    payment_amount DECIMAL(10,2),
    comments VARCHAR(500),
    rating INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (technician_id) REFERENCES Technician(technician_id),
    FOREIGN KEY (service_id) REFERENCES Service(service_id)
);

-- ========== STEP 3: Create Indexes ==========
CREATE INDEX idx_appointment_customer ON Appointment(customer_id);
CREATE INDEX idx_appointment_technician ON Appointment(technician_id);
CREATE INDEX idx_appointment_service ON Appointment(service_id);
CREATE INDEX idx_appointment_datetime ON Appointment(appointment_datetime);
CREATE INDEX idx_appointment_status ON Appointment(status);
CREATE INDEX idx_technician_available ON Technician(available);
CREATE INDEX idx_service_type ON Service(type);

-- ========== STEP 4: Insert Sample Data ==========

-- Manager (password = 'admin123' hashed with SHA-256)
INSERT INTO Manager (manager_id, name, email, password)
VALUES ('M-001', 'Manager Admin', 'admin@example.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9');

-- Technicians (password = 'password123' hashed with SHA-256)
INSERT INTO Technician (technician_id, name, email, specialty, available, password)
VALUES ('T-001', 'John Smith', 'john@example.com', 'Plumbing', TRUE, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f');

INSERT INTO Technician (technician_id, name, email, specialty, available, password)
VALUES ('T-002', 'Jane Doe', 'jane@example.com', 'Electrical', TRUE, 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f');

-- Customers
INSERT INTO Customer (customer_id, name, email, phone, address)
VALUES ('C-001', 'Alice Johnson', 'alice@example.com', '555-1234', '123 Main St');

INSERT INTO Customer (customer_id, name, email, phone, address)
VALUES ('C-002', 'Bob Wilson', 'bob@example.com', '555-5678', '456 Oak Ave');

-- Services
INSERT INTO Service (service_id, service_name, type, base_price)
VALUES ('S-001', 'Pipe Repair', 'Normal', 75.00);

INSERT INTO Service (service_id, service_name, type, base_price)
VALUES ('S-002', 'Complete System Upgrade', 'Major', 250.00);

-- ========== End of Schema ==========
