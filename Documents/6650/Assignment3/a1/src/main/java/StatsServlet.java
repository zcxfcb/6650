import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "SkierServlet")
public class StatsServlet extends HttpServlet {
  private static final String GET = "GET";
  private static final String POST = "POST";
  private final StatsDao statsDao = new StatsDao();

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    return;
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
//    System.out.println(urlPath);

    // check we have a URL!
    if (urlPath == null || urlPath.length() == 0) {
      sendUpdateFailureJson(res, HttpServletResponse.SC_BAD_REQUEST, GET);
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    if (urlPath.equals("/")) {
      String json = statsDao.getStats(SkierServlet.SKIER_POST_URL, POST);
//      String json = "1";
      if (json == null) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        sendUpdateFailureJson(res, HttpServletResponse.SC_NOT_FOUND, "Stats url" + SkierServlet.SKIER_POST_URL + " not found", GET);
      } else {
        sendUpdateSuccessJson(res, GET);
        res.getWriter().write(json);
      }
    } else {
      sendUpdateFailureJson(res, HttpServletResponse.SC_BAD_REQUEST, GET);
    }
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

