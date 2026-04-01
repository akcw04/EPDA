-- Database Schema for EPDA (Enterprise Programming - Dynamic Appointment System)
-- Target Database: Apache Derby
-- Run this script to create all required tables

-- ========== Drop existing tables (optional, for testing) ==========
-- DROP TABLE Appointment;
-- DROP TABLE Service;
-- DROP TABLE Technician;
-- DROP TABLE Customer;
-- DROP TABLE Manager;

-- ========== Create Manager Table ==========
CREATE TABLE Manager (
    manager_id VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== Create Technician Table ==========
CREATE TABLE Technician (
    technician_id VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    specialty VARCHAR(100) NOT NULL,
    available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== Create Customer Table ==========
CREATE TABLE Customer (
    customer_id VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== Create Service Table ==========
-- Type: 'Normal' (1 hour) or 'Major' (3 hours)
CREATE TABLE Service (
    service_id VARCHAR(30) PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,  -- 'Normal' or 'Major'
    base_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== Create Appointment Table ==========
-- Status: 'Pending', 'InProgress', 'Completed', 'Cancelled'
-- Uses COMPOSITION: contains objects, not just IDs
CREATE TABLE Appointment (
    appointment_id VARCHAR(30) PRIMARY KEY,
    customer_id VARCHAR(30) NOT NULL,
    technician_id VARCHAR(30) NOT NULL,
    service_id VARCHAR(30) NOT NULL,
    appointment_datetime TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',  -- Pending, InProgress, Completed, Cancelled
    payment_amount DECIMAL(10,2),
    comments VARCHAR(500),
    rating INTEGER,  -- 1-5 or null if not rated
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (technician_id) REFERENCES Technician(technician_id),
    FOREIGN KEY (service_id) REFERENCES Service(service_id)
);

-- ========== Create Indexes for Performance ==========
CREATE INDEX idx_appointment_customer ON Appointment(customer_id);
CREATE INDEX idx_appointment_technician ON Appointment(technician_id);
CREATE INDEX idx_appointment_service ON Appointment(service_id);
CREATE INDEX idx_appointment_datetime ON Appointment(appointment_datetime);
CREATE INDEX idx_appointment_status ON Appointment(status);
CREATE INDEX idx_technician_available ON Technician(available);
CREATE INDEX idx_service_type ON Service(type);

-- ========== Sample Data (Optional - for testing) ==========
-- Insert sample technicians
INSERT INTO Technician (technician_id, name, email, specialty, available) 
VALUES ('T-001', 'John Smith', 'john@example.com', 'Plumbing', TRUE);

INSERT INTO Technician (technician_id, name, email, specialty, available) 
VALUES ('T-002', 'Jane Doe', 'jane@example.com', 'Electrical', TRUE);

-- Insert sample customers
INSERT INTO Customer (customer_id, name, email, phone, address) 
VALUES ('C-001', 'Alice Johnson', 'alice@example.com', '555-1234', '123 Main St');

INSERT INTO Customer (customer_id, name, email, phone, address) 
VALUES ('C-002', 'Bob Wilson', 'bob@example.com', '555-5678', '456 Oak Ave');

-- Insert sample services
INSERT INTO Service (service_id, service_name, type, base_price) 
VALUES ('S-001', 'Pipe Repair', 'Normal', 75.00);

INSERT INTO Service (service_id, service_name, type, base_price) 
VALUES ('S-002', 'Complete System Upgrade', 'Major', 250.00);

-- Insert sample manager
INSERT INTO Manager (manager_id, name, email) 
VALUES ('M-001', 'Manager Admin', 'admin@example.com');

-- ========== End of Schema ==========
