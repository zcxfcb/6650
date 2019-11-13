
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {
  private static final String GET = "GET";
  private static final String POST = "POST";
  private final LiftRideDao liftRideDao = new LiftRideDao();
  private final StatsDao statsDao = new StatsDao();

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
//    System.out.println(urlPath);

    // check we have a URL!
    if (urlPath == null || urlPath.length() == 0 || urlPath.equals("/")) {
      sendUpdateFailureJson(res, HttpServletResponse.SC_NOT_FOUND, POST);
      return;
    }

    // Parse the request json body
    Integer liftID = null;
    Integer time = null;
    String line = req.getReader().readLine();
    String bodyJson = "";
    while (line != null) {
      bodyJson += line;
      line = req.getReader().readLine();
    }
//    System.out.println(bodyJson);
    JsonParser parser = new JsonParser();
    JsonElement element = parser.parse(bodyJson);
    JsonObject jsonObject = element.getAsJsonObject();
    liftID = jsonObject.get("liftID").getAsInt();
    time = jsonObject.get("time").getAsInt();

//    System.out.println("time: " + time);
//    System.out.println("liftID: " + liftID);
    req.getReader().close();

    // check the json body is valid
    // System.out.println(liftID + "," + time);
    if (liftID == null || time == null) {
      sendUpdateFailureJson(res, HttpServletResponse.SC_BAD_REQUEST, "Request body is invalid", POST);
      return;
    }

    // validate url path and return the response
    String[] urlParts = urlPath.split("/");
    if (isUrlValidSkierUpdate(urlParts)) {
      long before = System.currentTimeMillis();
      LiftRide ride = mapUrlToLiftRide(urlParts, liftID, time);
//      boolean dbWriteSuccess = true;
      boolean dbWriteSuccess = false;
      try {
        dbWriteSuccess = liftRideDao.createLiftRide(ride);
      } catch (NullPointerException e) {
        e.printStackTrace();
      }
      if (dbWriteSuccess) {
        sendUpdateSuccessJson(res, POST);
      } else {
        sendUpdateFailureJson(res, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to write to DB", POST);
      }
      long after = System.currentTimeMillis();
      statsDao.insertLatencyRecord(urlPath, after - before);
    } else {
      sendUpdateFailureJson(res, HttpServletResponse.SC_BAD_REQUEST, "Request URL is invalid", POST);
    }

  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
//    System.out.println(urlPath);

    // check we have a URL!
    if (urlPath == null || urlPath.length() == 0 || urlPath.equals("/")) {
      sendUpdateFailureJson(res, HttpServletResponse.SC_BAD_REQUEST, GET);
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    if (isUrlValidSkierUpdate(urlParts)) {
      // do any sophisticated processing with urlParts which contains all the url params
      // urlPath  = "/1/seasons/2019/day/1/skier/123"
      int resortID = Integer.valueOf(urlParts[1]);
      int seasonID = Integer.valueOf(urlParts[3]);
      int dayID = Integer.valueOf(urlParts[5]);
      int skierID = Integer.valueOf(urlParts[7]);
      String json = liftRideDao.queryLiftRideVertical(skierID, seasonID, resortID, dayID);
//      String json = "1";
      if (json == null) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        sendUpdateFailureJson(res, HttpServletResponse.SC_NOT_FOUND, "Entry not found", GET);
      } else {
        sendUpdateSuccessJson(res, GET);
        res.getWriter().write(json);
      }
    } else if (isUrlValidVerticalRequest(urlParts)) {
      int skierID = Integer.valueOf(urlParts[1]);
      String json = liftRideDao.queryLiftRideVerticalForSeason(skierID);
      if (json == null) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        sendUpdateFailureJson(res, HttpServletResponse.SC_NOT_FOUND, GET);
      } else {
        sendUpdateSuccessJson(res, GET);
        res.getWriter().write(json);
      }
    } else {
      sendUpdateFailureJson(res, HttpServletResponse.SC_BAD_REQUEST, GET);
    }
  }

  private String getJsonLineValue(String[] lineParts) {
    String str = lineParts[1].trim().substring(0, lineParts[1].length() - 1);
    str = str.charAt(str.length() - 1) == ','
        ? str.substring(0, str.length() - 1)
        : str;
    return str;
  }

  private boolean isUrlValidSkierUpdate(String[] urlPath) {
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    if (urlPath.length == 8) {
      return isNumeric(urlPath[1])
          && (urlPath[2].equals("season") || urlPath[2].equals("seasons"))
          && isNumeric(urlPath[3])
          && (urlPath[4].equals("days") || urlPath[4].equals("day"))
          && isNumeric(urlPath[5])
          && (urlPath[6].equals("skiers") || urlPath[6].equals("skier"))
          && isNumeric(urlPath[7]);
    }
    return false;
  }

  private LiftRide mapUrlToLiftRide(String[] urlPath) {
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    return new LiftRide(
        Integer.valueOf(urlPath[7]),
        Integer.valueOf(urlPath[1]),
        Integer.valueOf(urlPath[3]),
        Integer.valueOf(urlPath[5]),
        0,
        0);
  }

  private LiftRide mapUrlToLiftRide(String[] urlPath, int time, int liftID) {
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    return new LiftRide(
        Integer.valueOf(urlPath[7]),
        Integer.valueOf(urlPath[1]),
        Integer.valueOf(urlPath[3]),
        Integer.valueOf(urlPath[5]),
        time,
        liftID);
  }

  private boolean isUrlValidVerticalRequest(String[] urlPath) {
    // urlPath  = "/123/vertical"
    // urlParts = [, 123, vertical]
    if (urlPath.length == 3) {
      return isNumeric(urlPath[1]) && urlPath[2].equals("vertical");
    }
    return false;
  }

  private static boolean isNumeric(String str) {
    try {
      Integer.parseInt(str);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private void sendUpdateFailureJson(HttpServletResponse res, int responseCode, String httpMethod)
      throws IOException {
    sendUpdateFailureJson(res, responseCode, "failure", httpMethod);
  }

  private void sendUpdateFailureJson(HttpServletResponse res, int responseCode, String msg, String httpMethod)
      throws IOException {
    res.setStatus(responseCode);
    res.getWriter().write("{\"message\": \"" + msg + "\"}");
  }

  private void sendUpdateSuccessJson(HttpServletResponse res, String httpMethod) throws IOException {
    sendUpdateSuccessJson(res, null, httpMethod);
  }

  private void sendUpdateSuccessJson(HttpServletResponse res, String msg, String httpMethod) throws IOException {
    res.setStatus(HttpServletResponse.SC_OK);
    if (msg != null) {
      res.getWriter().write(new Gson().toJson(msg));
    }
  }
}

