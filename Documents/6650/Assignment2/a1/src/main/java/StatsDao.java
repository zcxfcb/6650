import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class StatsDao {
  public static final int MAX_RETRY = 10;
  private static final int updateStatementCountThreshold = 25600;
  private static final int maxStaySecondsInQueue = 1;
  private Timer timer;

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

  public boolean insertLatencyRecord(String url, float latency) {
    String insertQueryStatement = "INSERT INTO Latency (latency) " + "VALUES (?)";
    boolean isSuccess = false;
    int retryCount = 0;
    Connection conn = null;
    while (!isSuccess && retryCount < MAX_RETRY) {
      try {
        conn = connectToDB();
        PreparedStatement preparedStatement = conn.prepareStatement(insertQueryStatement);
        preparedStatement.setFloat(1, latency);
        preparedStatement.executeUpdate();
        isSuccess = true;
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        retryCount++;
        closeConnection(conn);
      }
    }
    return isSuccess;
  }

  public String getStats(String url, String operation) {
    String queryLatency
        = "SELECT "
        + "count(latency) as requestNum,"
        + "avg(latency) totalLatency,"
        + "max(latency) as maxLatency FROM Latency;";
    String queryStats
        = "SELECT requestNum, avgLatency, maxLatency FROM Stats WHERE url = " + url + ";";
    String updateStats
        = "UPDATE Stats"
        + "SET maxLatency = ?, avgLatency = ?, requestNum = ?"
        + "WHERE url = " + url + ";";
//    System.out.println(queryStatement);
    boolean isSuccess = false;
    int retryCount = 0;
    String output = null;
    Connection conn = null;
    while (!isSuccess && retryCount < MAX_RETRY) {
      try {
        int count = 0;
        float avg = 0;
        float max = 0;
        conn = connectToDB();
        // Get old stats
        PreparedStatement statsStatement = conn.prepareStatement(queryStats);
        ResultSet statsResultSet = statsStatement.executeQuery();
        if (statsResultSet.next()) {
          count = statsResultSet.getInt(1);
          avg = statsResultSet.getFloat(2);
          max = statsResultSet.getFloat(3);
        }

        // Get new stats and calculate combined stats
        PreparedStatement latencyStatements = conn.prepareStatement(queryLatency);
        ResultSet resultSet = latencyStatements.executeQuery();
        if (resultSet.next()) {
          int newCount = resultSet.getInt(1);
          int totalCount = count + newCount;
          avg = (resultSet.getFloat(2) * newCount / totalCount) + (avg * newCount / totalCount);
          count = totalCount;
          max = Math.max(max, resultSet.getFloat(3));
        }

        // Update Stats table
        PreparedStatement updateStatsStatement = conn.prepareStatement(updateStats);
        updateStatsStatement.setFloat(1, max);
        updateStatsStatement.setFloat(2, avg);
        updateStatsStatement.setFloat(3, count);

        // clear Latency table
        PreparedStatement latencyRemovalStatement = conn.prepareStatement("TRUNCATE TABLE Latency;");
        latencyRemovalStatement.executeQuery();

        output
            = "{ \"URL\": \"/resorts\","
            + "\"operation\": \"" + operation + "\","
            + "\"mean\": " + avg + ","
            + "\"max\": " + max
            + "}";
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        retryCount++;
        closeConnection(conn);
      }
    }
    return output == null ? "cannot get Stats" : output.toString();
  }
}