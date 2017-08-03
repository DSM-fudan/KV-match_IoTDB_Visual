package cn.edu.fudan.dsm.common;

/**
 * Created by dell on 2017/8/2.
 */
public class SimilarityResult {

    Long startTime;
    Long endTime;
    Double distance;

    public SimilarityResult() {
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "[" + startTime + "," + endTime + "," + distance + "]";
    }
}
