import java.sql.*;

public class LiftRideDao {
  private static Connection conn;
  private static final int MAX_RETRY = 1;

  private void connectToDB() throws SQLException {
    conn = DBCPDataSource.getConnection();
  }

  public void closeConnection() {
    try {
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public boolean createLiftRide(LiftRide newLiftRide) {
    String insertQueryStatement = "INSERT INTO LiftRides (skierId, resortId, seasonId, dayId, time, liftId) " +
        "VALUES (?,?,?,?,?,?)";
    boolean isSuccess = false;
    int retryCount = 0;
    while (!isSuccess && retryCount < MAX_RETRY) {
      try {
        connectToDB();
        PreparedStatement preparedStatement = conn.prepareStatement(insertQueryStatement);
        preparedStatement.setInt(1, newLiftRide.getSkierId());
        preparedStatement.setInt(2, newLiftRide.getResortId());
        preparedStatement.setInt(3, newLiftRide.getSeasonId());
        preparedStatement.setInt(4, newLiftRide.getDayId());
        preparedStatement.setInt(5, newLiftRide.getTime());
        preparedStatement.setInt(6, newLiftRide.getLiftId());
        // execute insert SQL statement
        preparedStatement.executeUpdate();
        isSuccess = true;
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        retryCount++;
        closeConnection();
      }
    }
    return isSuccess;
  }

  public String queyLiftRideVertical(int skierID) {
    String queryStatement
        = "SELECT seasonID, SUM(liftID) as vertical FROM LiftRides "
        + "WHERE skierID = " + skierID + " GROUP BY seasonID;";
    boolean isSuccess = false;
    int retryCount = 0;
    StringBuilder sb = new StringBuilder();
    while (!isSuccess && retryCount < MAX_RETRY) {
      try {
        connectToDB();
        PreparedStatement preparedStatement = conn.prepareStatement(queryStatement);
        // execute insert SQL statement
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
          sb.append("{\"seasonID\": \"" + resultSet.getInt(1) + "\",");
          sb.append("\"totalVert\": " + resultSet.getInt(2));
          sb.append("},");
        }
        System.out.println("sb:  " + sb.toString());
        if (sb.length() != 0) {
          sb.deleteCharAt(sb.length() - 1); // remove last comma
          sb.insert(0, "{\"resorts\":[");
          sb.append("]}");
          isSuccess = true;
        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        retryCount++;
        closeConnection();
      }
    }
    return sb.length() == 0 ? null : sb.toString();
  }

  public static void main(String[] args) {
    LiftRideDao liftRideDao = new LiftRideDao();
    liftRideDao.createLiftRide(new LiftRide(10, 2, 3, 5, 500, 20));
  }
}