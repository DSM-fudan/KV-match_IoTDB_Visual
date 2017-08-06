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
 * @author Ningting Pan
 */
@Service
public class QueryService {

    private final BasicDao basicDao;

    @Autowired
    public QueryService(BasicDao basicDao) {
        this.basicDao = basicDao;
    }

    public List<String> getPath() {
        return basicDao.getMetaData();
    }

    public List<SimilarityResult> query(KvMatchQueryRequest queryRequest) {
        List<SimilarityResult> results = basicDao.query(queryRequest);
        return results;
    }

    public List<TimeValue> getSeriesSimilar(Path path, Long startTime, Long endTime) {
        return basicDao.getSeriesSimilar(path, startTime, endTime);
    }

    public List<SimilarityResult> queryDraw(List<Pair<Integer, Double>> query, Path path, Double epsilon, Double alpha, Double beta) {
        List<SimilarityResult> results = basicDao.queryDraw(query, path, epsilon, alpha, beta);
        return results;
    }

    public String createIndex(String index_path) {
        return basicDao.createIndex(index_path);
    }
}
