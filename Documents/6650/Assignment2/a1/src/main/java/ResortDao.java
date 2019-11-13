import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResortDao {
  private static Connection conn;
  private static final int MAX_RETRY = 1;
  public String URL = null;
  public String trace = null;

  private void connectToDB() throws SQLException {
    conn = DBCPDataSource.getConnection();
    URL = DBCPDataSource.URL;
    trace = DBCPDataSource.trace;
  }

  public void closeConnection() {
    try {
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public boolean createResort(int resortID, int seasonID) {
    String insertQueryStatement = "INSERT INTO Resorts (resortId, seasonId) " + "VALUES (?,?)";
    boolean isSuccess = false;
    int retryCount = 0;
    while (!isSuccess && retryCount < MAX_RETRY) {
      try {
        connectToDB();
        PreparedStatement preparedStatement = conn.prepareStatement(insertQueryStatement);
        preparedStatement.setInt(1, resortID);
        preparedStatement.setInt(2, seasonID);
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

  public String queryResortSeason(int resortID) {
    String queryStatement
        = "SELECT seasonID FROM Resorts "
        + "WHERE resortID = " + resortID;
    return getJsonList("seasons", queryStatement);
  }

  public String queryResort() {
    String queryStatement
        = "SELECT resortID FROM Resorts";
    return getJsonList("resorts", queryStatement);
  }

  private String getJsonList(String listTitle, String queryStatement) {
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
          sb.append("\"" + resultSet.getInt(1) + "\",");
        }
//        System.out.println("sb:  " + sb.toString());
        if (sb.length() != 0) {
          sb.deleteCharAt(sb.length() - 1); // remove last comma
          sb.insert(0, "{\"" + listTitle + "\":[");
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
    ResortDao resortDao = new ResortDao();
//    resortDao.createResort(2, 2018);
//    resortDao.createResort(1, 2018);
//    resortDao.createResort(3, 2018);
//    resortDao.createResort(3, 2019);
//    System.out.println(resortDao.queryResortSeason(1));
//    System.out.println(resortDao.queryResort());
  }
}