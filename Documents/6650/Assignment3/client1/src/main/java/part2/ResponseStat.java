package part2;

public class ResponseStat {
  private long start;
  private long end;
  private int httpCode;

  public ResponseStat(long start, long end, int httpCode) {
    this.start = start;
    this.end = end;
    this.httpCode = httpCode;
  }

  public long getStart() {
    return start;
  }

  public long getEnd() {
    return end;
  }

  public long getLatency() {
    return end - start;
  }

  public int getHttpCode() {
    return httpCode;
  }
}
