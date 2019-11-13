package part1;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import org.apache.log4j.Logger;
import part2.ResponseStat;

public class SkierUpdateThread extends Thread {
  private int startID;
  private int endID;
  private int startTime;
  private int endTime;
  private int numRuns;
  private int numLifts;
  private CountDownLatch startLatch;
  private CountDownLatch countingLatch;
  private String urlBase;
  private Logger logger;
  private List<ResponseStat> stats;
  private int success;
  private int failure;

  public SkierUpdateThread(
      String urlBase,
      int startID,
      int endID,
      int startTime,
      int endTime,
      int numRuns,
      int numLifts,
      CountDownLatch latch1,
      CountDownLatch latch2,
      Logger logger) {
    this.startID = startID;
    this.endID = endID;
    this.startTime = startTime;
    this.endTime = endTime;
    //  (numRuns x 0.1) x (numSkiers / (numThreads / 4))
    this.numRuns = numRuns;
    this.numLifts = numLifts;
    this.startLatch = latch1;
    this.countingLatch = latch2;
    this.logger = logger;
    this.urlBase = urlBase;
    this.stats = new ArrayList<>(numRuns);
    this.success = 0;
    this.failure = 0;
  }

  public void run() {
    try {
      startLatch.await();
      SkiersApi apiInstance = new SkiersApi();
      ApiClient client = apiInstance.getApiClient();
      client.setBasePath(urlBase);
      for (int i = 0; i < numRuns; i++) {
        sendPost(apiInstance);
      }
    } catch (InterruptedException e) {
      logger.info("Thread has InterruptedException: " + e.getMessage());
      failure++;
      e.printStackTrace();
    } finally {
      if (countingLatch != null) countingLatch.countDown();
    }
  }

  private void sendPost(SkiersApi apiInstance) {
    Integer skierID = randomIntIntRange(startID, endID + 1);
    Integer resortID = randomIntIntRange(0, 10);
    Integer seasonID = randomIntIntRange(1, 4);
    Integer time = randomIntIntRange(startTime, endTime + 1);
    Integer liftID = randomIntIntRange(0, numLifts + 1);

    try {
      long before = System.currentTimeMillis();
      LiftRide body = (new LiftRide()).liftID(liftID);
      body.setTime(time);
      ApiResponse<Void> response = apiInstance.writeNewLiftRideWithHttpInfo(
          body,
          resortID,
          seasonID.toString(),
          time.toString(),
          skierID);
      ApiResponse<Integer> response2 = apiInstance.getSkierDayVerticalWithHttpInfo(
          resortID,
          seasonID.toString(),
          time.toString(),
          skierID);
      long after = System.currentTimeMillis();
      stats.add(new ResponseStat(before, after, response.getStatusCode()));
      if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
        success++;
      } else {
        failure++;
        logger.info("Call on API writeNewLiftRideWithHttpInfo failed with code: " + response.getStatusCode());
        System.out.println("Call on API writeNewLiftRideWithHttpInfo failed with code: " + response.getStatusCode());
      }
    } catch (ApiException e) {
      logger.info("ApiException: " + e.getMessage());
      System.out.println("ApiException: " + e.getMessage() + "\nbody: " + e.getResponseBody() + "\ncode: " + e.getCode());
      failure++;
      e.printStackTrace();
    } catch (Exception e) {
      logger.info("Exception: " + e.getMessage());
      failure++;
      e.printStackTrace();
    }
  }


  public static int randomIntIntRange(int min, int max) {
    Random r = new Random();
    return r.nextInt((max - min) + 1) + min;
  }

  public List<ResponseStat> getStats() {
    return stats;
  }

  public int getSuccess() {
    return success;
  }

  public int getFailure() {
    return failure;
  }
}
