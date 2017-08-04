package cn.edu.fudan.dsm.service;

import cn.edu.fudan.dsm.common.SimilarityResult;
import cn.edu.fudan.dsm.common.TimeValue;
import cn.edu.fudan.dsm.dao.BasicDao;
import cn.edu.thu.tsfile.common.utils.Pair;
import cn.edu.thu.tsfile.timeseries.read.qp.Path;
import cn.edu.thu.tsfiledb.index.kvmatch.KvMatchQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<SimilarityResult> query(KvMatchQueryRequest queryRequest) {
        List<SimilarityResult> results = basicDao.query(queryRequest);
        return results;
    }

    public List<TimeValue> getSeriesSimilar(Path path, Long startTime, Long endTime) {
        return basicDao.getSeriesSimilar(path, startTime, endTime);
    }

    public List<SimilarityResult> queryDraw(List<Pair<Integer, Double>> query, Path path, Double epsilon) {
        List<SimilarityResult> results = basicDao.queryDraw(query, path, epsilon);
        return results;
    }
}
