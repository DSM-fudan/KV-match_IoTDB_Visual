package cn.edu.fudan.dsm.common;

/**
 * @author Jiaye Wu
 */
public class TimeValue {

    private long time;

    private double value;

    public TimeValue(long time, double value) {
        this.time = time;
        this.value = value;
    }

    @Override
    public String toString() {
        return "[" + time + ", " + value + ']';
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
