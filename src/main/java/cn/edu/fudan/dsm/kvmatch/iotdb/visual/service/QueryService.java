package cn.edu.fudan.dsm.kvmatch.iotdb.visual.service;

import cn.edu.fudan.dsm.kvmatch.iotdb.visual.common.SimilarityResult;
import cn.edu.fudan.dsm.kvmatch.iotdb.visual.common.TimeValue;
import cn.edu.fudan.dsm.kvmatch.iotdb.visual.dao.BasicDao;
import cn.edu.tsinghua.iotdb.index.kvmatch.KvMatchQueryRequest;
import cn.edu.tsinghua.tsfile.common.utils.Pair;
import cn.edu.tsinghua.tsfile.timeseries.read.support.Path;
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
        return basicDao.query(queryRequest);
    }

    public List<TimeValue> getSeriesSimilar(Path path, Long startTime, Long endTime) {
        return basicDao.getSeriesSimilar(path, startTime, endTime);
    }

    public List<SimilarityResult> queryDraw(List<Pair<Integer, Double>> query, Path path, Double epsilon, Double alpha, Double beta) {
        return basicDao.queryDraw(query, path, epsilon, alpha, beta);
    }

    public void createIndex(String index_path) {
        basicDao.createIndex(index_path);
    }
}
