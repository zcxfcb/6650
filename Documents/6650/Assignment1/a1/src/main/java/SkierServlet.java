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

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
    System.out.println(urlPath);

    // check we have a URL!
    if (urlPath == null || urlPath.length() == 0 || urlPath.equals("/")) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      sendUpdateFailureJson(res, POST);
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    if (isUrlValidSkierUpdate(urlParts)) {
      sendUpdateSuccessJson(res, Integer.parseInt(urlParts[7]), POST);
    } else {
      sendUpdateFailureJson(res, POST);
    }
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
    System.out.println(urlPath);

    // check we have a URL!
    if (urlPath == null || urlPath.length() == 0 || urlPath.equals("/")) {
      sendUpdateFailureJson(res, GET);
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    if (isUrlValidSkierUpdate(urlParts)) {
      // do any sophisticated processing with urlParts which contains all the url params
      sendUpdateSuccessJson(res, Integer.parseInt(urlParts[7]), GET);
    } else if (isUrlValidVerticalRequest(urlParts)) {
      sendVerticalJson(res, Integer.parseInt(urlParts[1]), GET);
    } else {
      sendUpdateFailureJson(res, GET);
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

  private void sendUpdateFailureJson(HttpServletResponse res, String httpMethod) throws IOException {
    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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

