#select COUNT(concat(skierID, '-', seasonID) FROM LiftRides;
SELECT 
    skierID,
    seasonID,
    SUM(liftID) as vertical
FROM
    LiftRides
WHERE skierID = 1
GROUP BY seasonID;
# select * from LiftRides;
