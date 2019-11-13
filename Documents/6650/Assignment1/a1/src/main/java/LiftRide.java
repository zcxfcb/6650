public class LiftRide {
  private Integer skierId;
  private Integer resortId;
  private Integer seasonId;
  private Integer dayId;
  private Integer time;
  private Integer liftId;

  public LiftRide(Integer skierId, Integer resortId, Integer seasonId, Integer dayId, Integer time, Integer liftId) {
    this.skierId = skierId;
    this.resortId = resortId;
    this.seasonId = seasonId;
    this.dayId = dayId;
    this.time = time;
    this.liftId = liftId;
  }

  public Integer getSkierId() {
    return skierId;
  }

  public Integer getResortId() {
    return resortId;
  }

  public Integer getSeasonId() {
    return seasonId;
  }

  public Integer getDayId() {
    return dayId;
  }

  public Integer getTime() {
    return time;
  }

  public Integer getLiftId() {
    return liftId;
  }
}
