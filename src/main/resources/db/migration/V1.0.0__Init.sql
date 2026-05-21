CREATE TABLE simulation (
    id VARCHAR(36) PRIMARY KEY,
    principal_amount DECIMAL(18, 2) NOT NULL,
    interest_rate DECIMAL(18, 8) NOT NULL,
    duration_months INT NOT NULL,
    final_balance DECIMAL(18, 2) NOT NULL,
    total_interest DECIMAL(18, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE simulation_step (
    id VARCHAR(36) PRIMARY KEY,
    simulation_id VARCHAR(36) NOT NULL,
    month_number INT NOT NULL,
    initial_balance DECIMAL(18, 2) NOT NULL,
    interest_amount DECIMAL(18, 2) NOT NULL,
    final_balance DECIMAL(18, 2) NOT NULL,
    FOREIGN KEY (simulation_id) REFERENCES simulation(id) ON DELETE CASCADE
);

CREATE INDEX idx_simulation_step_parent ON simulation_step(simulation_id);
