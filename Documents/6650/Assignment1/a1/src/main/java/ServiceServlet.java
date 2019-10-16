import com.google.gson.Gson;
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

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
    System.out.println(urlPath);

    // check we have a URL!
    if (urlPath == null || urlPath.length() == 0) {
      sendUpdateFailureJson(res, POST);
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    if (isUrlValid(urlParts) && urlPath.length() == 3) {
      // do any sophisticated processing with urlParts which contains all the url params
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
    if (urlPath == null || urlPath.length() == 0) {
      sendUpdateFailureJson(res, GET);
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    if (isUrlValid(urlParts)) {
      // do any sophisticated processing with urlParts which contains all the url params
      sendUpdateSuccessJson(res, Integer.parseInt(urlParts[7]), GET);
    } else {
      sendUpdateFailureJson(res, GET);
    }
  }

  private boolean isUrlValid(String[] urlPath) {
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    if (urlPath.length == 1) {
      return true;
    } else if (urlPath.length == 3) {
      return isNumeric(urlPath[1]) && (urlPath[2].equals("season") || urlPath[2].equals("seasons"));
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
    res.getWriter().write(new Gson().toJson("failure"));
  }

  private void sendUpdateSuccessJson(HttpServletResponse res, Integer skierID, String httpMethod) throws IOException {
    res.setStatus(HttpServletResponse.SC_OK);
    res.getWriter().write(new Gson().toJson("123"));
  }
}

