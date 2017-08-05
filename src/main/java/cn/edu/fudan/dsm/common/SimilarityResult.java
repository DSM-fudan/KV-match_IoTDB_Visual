package cn.edu.fudan.dsm.common;

/**
 * @author Ningting Pan
 */
public class SimilarityResult {

    private long startTime;

    private long endTime;

    private double distance;

    public SimilarityResult() {
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "[" + startTime + "," + endTime + "," + distance + "]";
    }
}
