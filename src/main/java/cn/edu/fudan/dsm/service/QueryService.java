package cn.edu.fudan.dsm.service;

import cn.edu.fudan.dsm.common.TimeValue;
import cn.edu.fudan.dsm.dao.BasicDao;
import cn.edu.fudan.dsm.common.SimilarityResult;
import cn.edu.thu.tsfile.common.utils.Pair;
import cn.edu.thu.tsfile.timeseries.read.qp.Path;
import cn.edu.thu.tsfiledb.index.kvmatch.KvMatchQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 2017/8/1.
 */
@Service
public class QueryService {

    @Autowired
    BasicDao basicDao;

    public List<String> getPath() {
        return basicDao.getMetaData();
    }

    public long getSeriesLength(String path) {
        if (path == null || path.equals("")) return 0L;
        return basicDao.getSeriesLength(path);
    }

    public Pair<Integer,List<Pair<Long,Double>>> query(KvMatchQueryRequest queryRequest) {
        List<SimilarityResult> results = basicDao.query(queryRequest);
        List<Pair<Long, Double>> ans = new ArrayList<>();
        for (SimilarityResult result : results) {
            ans.add(new Pair<>(result.getStartTime(), result.getDistance()));
        }
        return new Pair<>(ans.size(), ans);
    }

    public List<TimeValue> getSeriesSimilar(Path path, Long startTime, Long endTime) {
        return basicDao.getSeriesSimilar(path, startTime, endTime);
    }
}
