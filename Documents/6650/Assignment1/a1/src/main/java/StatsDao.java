import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class LiftRideDao {
  public static final int MAX_RETRY = 10;
  private static final int updateStatementCountThreshold = 25600;
  private static final int maxStaySecondsInQueue = 1;
  private BlockingQueue<String> storedQueries = new ArrayBlockingQueue<>(updateStatementCountThreshold);
  private Timer timer;


  public LiftRideDao() {
    timer = new Timer();
    timer.schedule(new RemindTask(), maxStaySecondsInQueue*1000);
  }

  private Connection connectToDB() throws SQLException {
    Connection conn = DBCPDataSource.getConnection();
    return conn;
  }

  public void closeConnection(Connection conn) {
    try {
      if (conn != null) {
        conn.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public boolean createLiftRide(LiftRide newLiftRide) {
    String insertQueryStatement = "INSERT INTO LiftRides (skierId, resortId, seasonId, dayId, time, liftId) " +
        "VALUES ("
        + newLiftRide.getSkierId() + ","
        + newLiftRide.getResortId() + ","
        + newLiftRide.getSeasonId() + ","
        + newLiftRide.getDayId() + ","
        + newLiftRide.getTime() + ","
        + newLiftRide.getLiftId() + ")";
    synchronized (storedQueries) {
      storedQueries.add(insertQueryStatement);
      return batchUpdate(false);
    }
  }

  public boolean batchUpdate(boolean mustExecuteNow) {
    synchronized (storedQueries) {
      boolean isSuccess = false;
      int retryCount = 0;
      if (mustExecuteNow || storedQueries.size() >= updateStatementCountThreshold) {
        while (!isSuccess && retryCount < MAX_RETRY) {
          Connection conn = null;
          try {
            // batch add queries to statements
            conn = connectToDB();
            Statement updateStatement = conn.createStatement();
            for (String query : storedQueries) {
              updateStatement.addBatch(query);
            }
            // execute insert SQL statement
            updateStatement.executeBatch();
            isSuccess = true;
          } catch (SQLException e) {
            e.printStackTrace();
            retryCount++;
          } finally {
            closeConnection(conn);
          }
        }
//        System.out.println("clear queue before:" + storedQueries.size());
        storedQueries.clear();
//        System.out.println("clear queue after:" + storedQueries.size());
        return isSuccess;
      } else {
        // query is stored
        return true;
      }
    }
  }

  public String queryLiftRideVerticalForSeason(int skierID) {
    String queryStatement
        = "SELECT seasonID, SUM(liftID) as vertical FROM LiftRides "
        + "WHERE skierID = " + skierID + " GROUP BY seasonID;";
    boolean isSuccess = false;
    int retryCount = 0;
    StringBuilder sb = new StringBuilder();
    while (!isSuccess && retryCount < MAX_RETRY) {
      Connection conn = null;
      try {
        conn = connectToDB();
        PreparedStatement preparedStatement = conn.prepareStatement(queryStatement);
        ResultSet resultSet = preparedStatement.executeQuery();
        sb = new StringBuilder();
        while (resultSet.next()) {
          sb.append("{\"seasonID\": \"" + resultSet.getInt(1) + "\",");
          sb.append("\"totalVert\": " + resultSet.getInt(2));
          sb.append("},");
        }
//        System.out.println("sb:  " + sb.toString());
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
        closeConnection(conn);
      }
    }
    return sb.length() == 0 ? null : sb.toString();
  }

  public String queryLiftRideVertical(int skierID, int seasonID, int resortID, int dayID) {
    String queryStatement
        = "SELECT SUM(liftID) as vertical FROM LiftRides"
        + " WHERE skierID = " + skierID
        + " AND seasonID = " + seasonID
        + " AND resortID = " + resortID
        + " AND dayID = " + dayID
        + ";";
//    System.out.println(queryStatement);
    boolean isSuccess = false;
    int retryCount = 0;
    Integer output = -1;
    Connection conn = null;
    while (!isSuccess && retryCount < MAX_RETRY) {
      try {
        conn = connectToDB();
        PreparedStatement preparedStatement = conn.prepareStatement(queryStatement);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
          output = resultSet.getInt(1);
        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        retryCount++;
        closeConnection(conn);
      }
    }
    return output == -1 ? null : output.toString();
  }

  public static void main(String[] args) {
    LiftRideDao liftRideDao = new LiftRideDao();
    liftRideDao.createLiftRide(new LiftRide(10, 2, 3, 5, 500, 20));
  }

  private void restartTimer() {
    if (timer != null) {
      timer.cancel(); //Terminate the timer thread
    }
    timer = new Timer();
    timer.schedule(new RemindTask(), maxStaySecondsInQueue*1000);
  }

  class RemindTask extends TimerTask {
    public void run() {
      if (!storedQueries.isEmpty()) {
        batchUpdate(true);
      }
      restartTimer();
    }
  }
}