#DROP TABLE LiftRides;
#DROP TABLE Resorts;

CREATE TABLE LiftRides.LiftRides(
id INT auto_increment primary key,
skierID INT,
time INT,
liftID INT,
resortID INT,
seasonID INT,
dayID INT,
INDEX SKIER_SEASON_INDEX (skierId, seasonID)
);

CREATE TABLE LiftRides.Resorts(
resortID VARCHAR(4),
seasonID INT,
PRIMARY KEY (resortID, seasonID)
);