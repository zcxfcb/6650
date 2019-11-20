package part2;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import part1.SkierUpdateThread;

public class MetricsReporter {
  public final static String SEPARATOR = ",";
  public final static String HTTP_METHOD = "POST";

  private long wallTime;
  private int requestNum;
//  mean response time (millisecs)
//  median response time (millisecs)
//  throughput = total number of requests/wall time
//  p99 (99th percentile) response time.
//  max response time
  private List<ResponseStat> allStats;
  private double mean;
  private double median;
  private double throughput;
  private double p99;
  private double max;

  public MetricsReporter(BlockingQueue<SkierUpdateThread> threadList, long wallTime, int requestNum) {
    allStats = new ArrayList<>(threadList.size() * threadList.peek().getStats().size());
    for (SkierUpdateThread thread : threadList) {
      allStats.addAll(thread.getStats());
    }
    this.wallTime = wallTime;
    this.requestNum = requestNum;
  }

  public void calculateMetrics() {
    if (allStats.size() > 0) {
      Collections.sort(allStats, (a, b) -> (int) (a.getLatency() - b.getLatency()));
      this.mean = allStats.stream().mapToLong(ResponseStat::getLatency).average().getAsDouble();
      this.median = allStats.size() % 2 == 0 ?
          (allStats.get(allStats.size() / 2 - 1).getLatency() + allStats.get(allStats.size() / 2).getLatency()) / 2.0
          : allStats.get(allStats.size() / 2).getLatency();
      this.throughput = ((double) wallTime) / requestNum;
      this.max = allStats.get(allStats.size() - 1).getLatency();

      long sum = allStats.stream().mapToLong(ResponseStat::getLatency).sum();
      double cut = sum * 0.99;
      sum = allStats.get(0).getLatency();
      int i = 0;
      while (sum < cut) {
        i++;
        sum += allStats.get(i).getLatency();
      }
      this.p99 = allStats.get(i).getLatency();
    } else {
      System.out.println("No record of successful update processed in part 2.");
    }
  }

  public void printMetrics() {
    System.out.println("-----------------Part 2 Report------------------");
    System.out.println("mean response time (millisecs): " + mean);
    System.out.println("median response time (millisecs): " + median);
    System.out.println("throughput (total number of requests/wall time): " + throughput);
    System.out.println("p99 response time (99th percentile, millisecs): " + p99);
    System.out.println("max response time(millisecs): " + max);
  }

  public void writeToFile(String outputFilename) {
    try {
      FileWriter fileWriter = new FileWriter(outputFilename);
      PrintWriter printWriter = new PrintWriter(fileWriter);
      printWriter.printf("start time,request type (ie POST),latency,response code\n");
      for (ResponseStat stat : allStats) {
        //{start time, request type (ie POST), latency, response code}
        String str = stat.getStart() + SEPARATOR + HTTP_METHOD + SEPARATOR + stat.getLatency() + SEPARATOR + stat.getHttpCode();
        printWriter.printf("%s\n", str);
      }
      printWriter.close();
    } catch (FileNotFoundException ex) {
      System.out.println(
          "Unable to open file '" +
              outputFilename + "'");
    } catch (IOException ex) {
      System.out.println(
          "Error writing to file '"
              + outputFilename + "'");
    }
  }
}
