package cn.edu.fudan.dsm.common;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dell on 2017/8/2.
 */
public class Series {

    List<TimeValue> values;

    public Series(List<TimeValue> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return Arrays.toString(getValues().toArray());
    }

    public List<TimeValue> getValues() {
        return values;
    }

    public void setValues(List<TimeValue> values) {
        this.values = values;
    }
}
