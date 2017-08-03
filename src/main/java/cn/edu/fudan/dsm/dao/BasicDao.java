package cn.edu.fudan.dsm.dao;

import cn.edu.fudan.dsm.common.SimilarityResult;
import cn.edu.fudan.dsm.common.TimeValue;
import cn.edu.thu.tsfile.timeseries.read.qp.Path;
import cn.edu.thu.tsfiledb.index.kvmatch.KvMatchQueryRequest;
import cn.edu.thu.tsfile.common.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 2017/8/1.
 */
@Repository
public class BasicDao {

    private static final Logger logger = LoggerFactory.getLogger(BasicDao.class);

    private static final String SELECT_MATCHING_SQL = "select index subsequence_matching(%s, %s, %s, %s, %s, %s, %s)"; // X, Q startTime endTime epsilon alpha beta
    private static final String SELECT_MATCHING_SERIES_SQL = "select * from %s where time >= %s and time <= %s";
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BasicDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> getMetaData() {
        ConnectionCallback<Object> connectionCallback = new ConnectionCallback<Object>() {
            public Object doInConnection(Connection connection) throws SQLException {
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                ResultSet resultSet = databaseMetaData.getColumns(null, null, "root.*", null);
                List<String> columnsName = new ArrayList<>();
                while(resultSet.next()){
                    columnsName.add(resultSet.getString(0));
                    //System.out.println(String.format("column %s", resultSet.getString(0)));
                }
                return columnsName;
            }
        };
        return (List<String>)jdbcTemplate.execute(connectionCallback);
    }

    public long getSeriesLength(String path) {
        Long len = jdbcTemplate.queryForObject("select count(*) from " + path, Long.class);
        return len;
    }

    public List<SimilarityResult> query(KvMatchQueryRequest queryRequest) {
        Path path = queryRequest.getColumnPath();
        Path queryPath = queryRequest.getQueryPath();
        Long startTime = queryRequest.getQueryStartTime();
        Long endTime = queryRequest.getQueryEndTime();
        double epsilon = queryRequest.getEpsilon();
        double alpha = queryRequest.getAlpha();
        double beta = queryRequest.getBeta();
        String sql = String.format(SELECT_MATCHING_SQL, path, queryPath, startTime, endTime, epsilon, alpha, beta);
        List<SimilarityResult> similarityResults = jdbcTemplate.query(sql, new RowMapper<SimilarityResult>() {
            @Override
            public SimilarityResult mapRow(ResultSet rs, int rowNum) throws SQLException {
                SimilarityResult similarityResult = new SimilarityResult();
                similarityResult.setStartTime(rs.getLong(1));
                similarityResult.setEndTime(rs.getLong(2));
                similarityResult.setDistance(rs.getDouble(3));
                return similarityResult;
            }
        });
        return similarityResults;
    }

    public List<TimeValue> getSeriesSimilar(Path path, Long startTime, Long endTime) {
        String sql = String.format(SELECT_MATCHING_SERIES_SQL, path, startTime, endTime);
        List<TimeValue> data = jdbcTemplate.query(sql, new RowMapper<TimeValue>() {
            @Override
            public TimeValue mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new TimeValue(rs.getLong(0), rs.getDouble(1));
            }
        });
        return data;
    }
}
