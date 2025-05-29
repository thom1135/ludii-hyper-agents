CREATE TABLE evaluations (
    evaluation_id INT AUTO_INCREMENT PRIMARY KEY,
    game_name VARCHAR(255) NOT NULL,
    agent_name VARCHAR(255) NOT NULL,
    game_index INTEGER NOT NULL,
    agent_index INTEGER NOT NULL,
    games_played INTEGER NOT NULL,
    score FLOAT NOT NULL,
    mean FLOAT NOT NULL,
    standard_deviation FLOAT NOT NULL,
    variance FLOAT NOT NULL,
    duration FLOAT NOT NULL,
    natural_end FLOAT NOT NULL,
    move_limit FLOAT NOT NULL,
    turn_limit FLOAT NOT NULL,
    supported BOOLEAN NOT NULL,
    selected_heuristic VARCHAR(255) NOT NULL,
    selected_agent VARCHAR(255) NOT NULL,
    ensemble_size INTEGER NOT NULL,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE game_result (
    result_id INT AUTO_INCREMENT PRIMARY KEY,
    game_name VARCHAR(255) NOT NULL,
    agent_name VARCHAR(255) NOT NULL,
    iteration INTEGER NOT NULL,
    score FLOAT NOT NULL,
    duration FLOAT NOT NULL,
    natural_end BOOLEAN NOT NULL,
    move_limit BOOLEAN NOT NULL,
    turn_limit FLOAT NOT NULL,
    supported BOOLEAN NOT NULL,
    selected_heuristic VARCHAR(255) NOT NULL,
    selected_agent VARCHAR(255) NOT NULL,
    ensemble_size INTEGER NOT NULL,
    complete_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE skip (
    skip_id INT AUTO_INCREMENT PRIMARY KEY,
    container_index INT NOT NULL,
    game_name VARCHAR(255) NOT NULL,
    game_index INTEGER NOT NULL,
    exception BOOLEAN NOT NULL,
    skipped_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);