DROP TABLE LiftRides;

# version 1

CREATE TABLE LiftRides(
id INT auto_increment primary key,
skierID INT,
time INT,
liftID INT,
resortID INT,
seasonID INT,
dayID INT,
INDEX SKIER_SEASON_INDEX (skierId, seasonID)
);

# version 2

CREATE TABLE LiftRides(
composite VARCHAR(40) primary key,
skierID INT,
time INT,
liftID INT,
resortID INT,
seasonID INT,
dayID INT
);