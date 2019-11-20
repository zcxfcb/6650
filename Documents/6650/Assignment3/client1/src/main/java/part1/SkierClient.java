package part1;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import part2.MetricsReporter;

public class SkierClient {
  private static int MAX_NUM_OF_THREAD = 1000;
  private static int MIN_NUM_OF_THREAD = 5;
  private static int MAX_NUM_OF_SKIERS = 50000;
  private static int MIN_NUM_OF_LIFT_PER_SKIER = 5;
  private static int MAX_NUM_OF_LIFT_PER_SKIER = 60;
  private static int MAX_NUM_OF_RUNS = 20;
  private static int TIME_OUT = 30;

  private static int DEFAULT_NUM_OF_THREAD = 256;
  private static int DEFAULT_NUM_OF_SKIERS = 256;
  private static int DEFAULT_NUM_OF_LIFT_PER_SKIER = 40;
  private static int DEFAULT_NUM_OF_RUNS = 20;

  private static String ip = "ec2-user@ec2-18-206-147-205.compute-1.amazonaws.com";
//  private static String ip = "localhost";
  private static Integer port = 8080;
  private static String webapp = "a1_war";
//  private static String webapp = "";
  private static String URL = "http://" + ip + ":" + port + "/" + webapp;
  private static String outputFile = "output/output-1000thread.csv";

  private static int numThread = DEFAULT_NUM_OF_THREAD;
  private static int numSkiers = DEFAULT_NUM_OF_SKIERS;
  private static int numLifts = DEFAULT_NUM_OF_LIFT_PER_SKIER;
  private static int numRuns = DEFAULT_NUM_OF_RUNS;

  private static Logger logger = Logger.getLogger(Thread.class.getName());

  /**
   * maximal number of threads to run (numThreads - max 256)
   * number of skier to generate lift rides for (numSkiers - max 50000), This is effectively the skier’s ID (skierID)
   * number of ski lifts (numLifts - range 5-60, default 40)
   * mean numbers of ski lifts each skier rides each day (numRuns - default 10, max 20)
   * IP/port address of the server
   */
  public static void main(String[] args) {
//    if (args.length == 2) {
//      hostName = args[0];
//      portNumber = Integer.parseInt(args[1]);
//    } else if (args.length == 6) {
//      ip = args[0];
//      port = Integer.parseInt(args[1]);
//      validateParams(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
//      numThread = Integer.parseInt(args[2]);
//      numSkiers = Integer.parseInt(args[3]);
//      numLifts = Integer.parseInt(args[4]);
//      numRuns = Integer.parseInt(args[5]);
//    } else {
//      System.err.println("Usage: java part1.SkierClient <host name> <port number> "
//          + "or java part1.SkierClient <host name> <port number> <numThreads> <numSkiers> <numLifts> <numRuns>");
//      System.exit(1);
//    }

    // hard code the params for the ease of stress test
    if (!validateParams(numThread, numSkiers, numLifts, numRuns)) {
      throw new IllegalArgumentException(
          "Unable to create part1.SkierClient due to invalid parameters. Please see requirement:\n"
              + "maximal number of threads to run (numThreads - max 256)\n"
              + "number of skier to generate lift rides for (numSkiers - max 50000)\n"
              + "number of ski lifts (numLifts - range 5-60, default 40)\n"
              + "mean numbers of ski lifts each skier rides each day (numRuns - default 10, max 20)\n"
      );
    }
    int factor = 3600;
    int numThreadPhase1 = numThread / 4;
    int numThreadPhase2 = numThread;
    int numThreadPhase3 = numThread / 4;
    int timeStartPhase1 = 0 * factor;
    int timeEndPhase1 = 90 * factor;
    int timeStartPhase2 = 91 * factor;
    int timeEndPhase2 = 360 * factor;
    int timeStartPhase3 = 361 * factor;
    int timeEndPhase3 = 420 * factor;
    int numRunsPerThreadPhase1 = numRuns * numSkiers / 10 / numThreadPhase1;
    int numRunsPerThreadPhase2 = numRuns * numSkiers * 8 / 10 / numThreadPhase2;
    int numRunsPerThreadPhase3 = numRuns * numSkiers / 10 / numThreadPhase3;
    CountDownLatch latch1 = new CountDownLatch(0);
    CountDownLatch latch2 = new CountDownLatch(numThread / 4 / 10);
    CountDownLatch latch3 = new CountDownLatch(numThread / 10);
    BlockingQueue<SkierUpdateThread> allThreads = new ArrayBlockingQueue<>(numThread * 2);


    // start phases
    try {
      long start = System.currentTimeMillis();
      ExecutorService executorService1 =  createPhase(
          numThreadPhase1,
          numSkiers,
          timeStartPhase1,
          timeEndPhase1,
          numRunsPerThreadPhase1,
          numLifts,
          latch1,
          latch2,
          allThreads);
      ExecutorService executorService2 =  createPhase(
          numThreadPhase2,
          numSkiers,
          timeStartPhase2,
          timeEndPhase2,
          numRunsPerThreadPhase2,
          numLifts,
          latch2,
          latch3,
          allThreads);
      ExecutorService executorService3 =  createPhase(
          numThreadPhase3,
          numSkiers,
          timeStartPhase3,
          timeEndPhase3,
          numRunsPerThreadPhase3,
          numLifts,
          latch3,
          null,
          allThreads);
      executorService1.awaitTermination(TIME_OUT, TimeUnit.MINUTES);
      executorService2.awaitTermination(TIME_OUT, TimeUnit.MINUTES);
      executorService3.awaitTermination(TIME_OUT, TimeUnit.MINUTES);
      long end = System.currentTimeMillis();
      long wallTime = end - start;
      int successCount = 0;
      int failureCount = 0;
      for (SkierUpdateThread thread : allThreads) {
        successCount += thread.getSuccess();
        failureCount += thread.getFailure();
      }
      System.out.println("-----------------Part 1 Report------------------");
      System.out.println("number of successful requests sent: " + successCount);
      System.out.println("number of unsuccessful requests sent: " + failureCount);
      System.out.println("Wall time: " + wallTime);
      System.out.println("Number of threads: " + allThreads.size());

      MetricsReporter metricsReporter = new MetricsReporter(allThreads, wallTime, numSkiers * numRuns);
      metricsReporter.writeToFile(outputFile);
      metricsReporter.calculateMetrics();
      metricsReporter.printMetrics();
    } catch (InterruptedException e) {
      logger.info("ExecutorService was interrupted with message: " + e.getMessage());
    }
  }

  private static ExecutorService createPhase(
      int threadNum,
      int skierNum,
      int startTime,
      int endTime,
      int runNum,
      int liftNum,
      CountDownLatch startLatch,
      CountDownLatch countingLatch,
      BlockingQueue<SkierUpdateThread> allThreads) {
    int skierRange = skierNum/threadNum;
    ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
    for (int i = 0; i < threadNum; i++) {
      SkierUpdateThread thread = new SkierUpdateThread(
          URL,
          i * skierRange + 1,
          (i + 1) * skierRange,
          startTime,
          endTime,
          runNum,
          liftNum,
          startLatch,
          countingLatch,
          logger);
      executorService.execute(thread);
      allThreads.add(thread);
    }
    executorService.shutdown();
    return executorService;
  }

  private static boolean validateParams(int numThreads, int numSkiers, int numLifts, int numRuns) {
    return numThreads > MIN_NUM_OF_THREAD && numThreads <= MAX_NUM_OF_THREAD
        && numSkiers >= 0 && numSkiers <= MAX_NUM_OF_SKIERS
        && numLifts >= MIN_NUM_OF_LIFT_PER_SKIER && numLifts <= MAX_NUM_OF_LIFT_PER_SKIER
        && numRuns >= 0 && numRuns <= MAX_NUM_OF_RUNS;
  }
}


