import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import org.apache.commons.dbcp2.*;

public class DBCPDataSource {
  private static BasicDataSource dataSource = new BasicDataSource();
  public static String URL = null;
  public static String user = null;
  public static String pwd = null;
  public static String trace = null;

  // NEVER store sensitive information below in plain text!
//  private static final String HOST_NAME = System.getProperty("MySQL_IP_ADDRESS");
//  private static final String PORT = System.getProperty("MySQL_PORT");
//  private static final String DATABASE = "LiftRides";
//  private static final String USERNAME = System.getProperty("DB_USERNAME");
//  private static final String PASSWORD = System.getProperty("DB_PASSWORD");

//  private static final String HOST_NAME = System.getenv("MySQL_IP_ADDRESS");
//  private static final String PORT = System.getenv("MySQL_PORT");
//  private static final String DATABASE = "LiftRides";
//  private static final String USERNAME = System.getenv("DB_USERNAME");
//  private static final String PASSWORD = System.getenv("DB_PASSWORD");

  private static final String HOST_NAME = "localhost";
  private static final String PORT = "3306";
  private static final String DATABASE = "LiftRides";
  private static final String USERNAME = "root";
  private static final String PASSWORD = "Zerket11";

  static {
    // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-jdbc-url-format.html
    String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
//    System.out.println(url);
    URL = url;
    user = USERNAME;
    pwd = PASSWORD;
    dataSource.setUrl(url);
    dataSource.setUsername(USERNAME);
    dataSource.setPassword(PASSWORD);
    dataSource.setMinIdle(5);
    dataSource.setMaxIdle(10);
    dataSource.setMaxOpenPreparedStatements(100);
  }

  public static Connection getConnection() throws SQLException {

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      return dataSource.getConnection();
    } catch (ClassNotFoundException e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      trace = sw.toString();
    }
    return null;
  }
}
