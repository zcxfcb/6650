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
  private LiftRideDao liftRideDao = new LiftRideDao();

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
//    System.out.println(urlPath);

    // check we have a URL!
    if (urlPath == null || urlPath.length() == 0 || urlPath.equals("/")) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      sendUpdateFailureJson(res, HttpServletResponse.SC_BAD_REQUEST, POST);
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    if (isUrlValidSkierUpdate(urlParts)) {
      sendUpdateSuccessJson(res, Integer.parseInt(urlParts[7]), POST);
    } else {
      sendUpdateFailureJson(res,  HttpServletResponse.SC_BAD_REQUEST, POST);
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
      LiftRide ride = mapUrlToLiftRide(urlParts);
      boolean dbWriteSuccess = liftRideDao.createLiftRide(ride);
      if (dbWriteSuccess) {
        sendUpdateSuccessJson(res, Integer.valueOf(urlParts[7]), GET);
      } else {
        sendUpdateFailureJson(res, HttpServletResponse.SC_SERVICE_UNAVAILABLE, GET);
      }
    } else if (isUrlValidVerticalRequest(urlParts)) {
      sendVerticalJson(res, Integer.parseInt(urlParts[1]), GET);
    } else {
      sendUpdateFailureJson(res, HttpServletResponse.SC_BAD_REQUEST, GET);
    }
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
        null,
        null);
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
    res.setStatus(responseCode);
    String message = "{\"skier update\" : \"failure\","
        + "\"Request\" : \""+ httpMethod + "\"}";
    res.getWriter().write(new Gson().toJson("failure"));
  }

  private void sendUpdateSuccessJson(HttpServletResponse res, Integer skierID, String httpMethod) throws IOException {
    res.setStatus(HttpServletResponse.SC_OK);
    String message = "{\"skier update\" : \"success\","
        + "\"Request\" : \"" + httpMethod + "\","
        + "\"skier ID\" : " + skierID + "}";
    res.getWriter().write(new Gson().toJson("123"));
  }

  private void sendVerticalJson(HttpServletResponse res, Integer skierID, String httpMethod) throws IOException {
    String message = "{\"skier vertical\" : \"success\","
        + "\"Request\" : \""+ httpMethod + "\","
        + "\"skier ID\" : " + skierID + "}";
    res.setStatus(HttpServletResponse.SC_OK);
    res.getWriter().write(new Gson().toJson("vertical"));
  }
}

