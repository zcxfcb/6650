import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "SkierServlet")
public class ServiceServlet extends HttpServlet {
  private static final String GET = "GET";
  private static final String POST = "POST";
  private ResortDao resortDao = new ResortDao();

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
//    System.out.println(urlPath);

    // check we have a URL!
    if (urlPath == null || urlPath.length() == 0) {
      sendUpdateFailureJson(res, SC_BAD_REQUEST, "empty url path after /resort", POST);
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    if (isUrlValidUpdate(urlParts)) {
      // Parse the request json body
      Integer season = null;
      String line = req.getReader().readLine();
      String bodyJson = "";
      while (line != null) {
        bodyJson += line;
        line = req.getReader().readLine();
      }
//      System.out.println(bodyJson);
      JsonParser parser = new JsonParser();
      JsonElement element = parser.parse(bodyJson);
      JsonObject jsonObject = element.getAsJsonObject();
      season = jsonObject.get("season").getAsInt();
//      System.out.println("season: " + season);
      req.getReader().close();

      if (season == -1) {
        sendUpdateFailureJson(res, SC_BAD_REQUEST, "no parameter year in post request body", POST);
        return;
      }
      // update database
      resortDao.createResort(Integer.valueOf(urlParts[1]), season);
      sendUpdateSuccessJson(res, POST);
    } else {
      sendUpdateFailureJson(res, SC_BAD_REQUEST, "Url is not valid", POST);
    }
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
//    System.out.println(urlPath);

    // check we have a URL!
    if (urlPath == null || urlPath.length() == 0) {
      sendUpdateFailureJson(res, SC_BAD_REQUEST,  GET);
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    try {
      if (urlPath.equals("/")) {
        // get all resorts
        String json = resortDao.queryResort();
        sendUpdateSuccessJson(res, json, GET);
      } else if (isUrlValidUpdate(urlParts)) {
        // get list of seasons for an resort id
        String json = resortDao.queryResortSeason(Integer.valueOf(urlParts[1]));
        sendUpdateSuccessJson(res, json, GET);
      } else {
        sendUpdateFailureJson(res, SC_BAD_REQUEST, "Invalid url", GET);
      }
    } catch (NullPointerException e) {
      sendUpdateFailureJson(res, SC_BAD_REQUEST, "connection error\ndb url:\n" +DBCPDataSource.URL + "\nuser and pwd:\n" + DBCPDataSource.user + " and " + DBCPDataSource.pwd + "\ntrace:\n" + resortDao.trace, GET);
    }
  }

  private boolean isUrlGetAllResort(String[] urlPath) {
    // urlPath  = "/"
    // urlParts = empty array
    return urlPath.length == 0;
  }

  private boolean isUrlValidUpdate(String[] urlPath) {
    // urlPath  = "/1/seasons"
    // urlParts = [, 1, seasons]
    if (urlPath.length == 3) {
      return isNumeric(urlPath[1])
          && (urlPath[2].equals("season") || urlPath[2].equals("seasons"));
    }
    return false;
  }

  private static boolean isNumeric(String str) {
    try {
      Integer.valueOf(str);
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
    res.setStatus(SC_OK);
    if (msg != null) {
      res.getWriter().write(msg);
    }
  }

  private String getJsonLineValue(String[] lineParts) {
    String str = lineParts[1].trim().substring(0, lineParts[1].length() - 1);
    str = str.charAt(str.length() - 1) == ','
        ? str.substring(0, str.length() - 1)
        : str;
    return str;
  }
}

