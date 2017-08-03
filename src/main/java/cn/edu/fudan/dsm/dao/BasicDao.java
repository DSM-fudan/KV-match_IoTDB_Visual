package cn.edu.fudan.dsm.dao;

import cn.edu.fudan.dsm.common.SimilarityResult;
import cn.edu.fudan.dsm.common.TimeValue;
import cn.edu.thu.tsfile.common.utils.Pair;
import cn.edu.thu.tsfile.timeseries.read.qp.Path;
import cn.edu.thu.tsfiledb.index.kvmatch.KvMatchQueryRequest;
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
    private static final String SELECT_SERIES_SQL = "select * from %s where time >= %s and time <= %s";
    private static final String CREATE_TEMP_SERIES_SQL = "create timeseries %s with datatype=DOUBLE,encoding=RLE"; //path + suffix
    private static final String DELETE_TEMP_SERIES_SQL = "delete timeseries %s";
    private static final String INSERT_TEMP_SERIES_SQL = "insert into %s(timestamp,%s) values (%s,%s)"; //device sensor time value
    private static final String suffix = "_tmp_index";
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
        return getSimilarityResult(queryRequest);
    }

    private List<SimilarityResult> getSimilarityResult(KvMatchQueryRequest queryRequest) {
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
        String sql = String.format(SELECT_SERIES_SQL, path, startTime, endTime);
        List<TimeValue> data = jdbcTemplate.query(sql, new RowMapper<TimeValue>() {
            @Override
            public TimeValue mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new TimeValue(rs.getLong(0), rs.getDouble(1));
            }
        });
        return data;
    }

    private KvMatchQueryRequest createTempSeries(Path path, List<Pair<Integer, Double>> query, double epsilon) {
        String pathStr = path.getFullPath();
        String device = pathStr.substring(0, pathStr.lastIndexOf("."));
        System.out.println("pathStr:"+pathStr);
        String tmp_series_name = pathStr+suffix;
        String sensor = pathStr.substring(pathStr.lastIndexOf(".") + 1, pathStr.length()) + suffix;
        // create temp table
        String sql = String.format(CREATE_TEMP_SERIES_SQL, tmp_series_name);
        logger.debug(sql);
        try {
            jdbcTemplate.execute(sql);
            // insert Q values
            long startTime = 0L, endTime = 0L;
            for (int i = 0; i < query.size(); i++) {
                Pair<Integer, Double> q = query.get(i);
                if (i == 0) startTime = (long) q.left;
                else if (i == query.size() - 1) endTime = (long) q.left;
                sql = String.format(INSERT_TEMP_SERIES_SQL, device, sensor, q.left, q.right);
                jdbcTemplate.update(sql);
            }
            // create index for Q
            return KvMatchQueryRequest.builder(path, new Path(tmp_series_name), startTime, endTime, epsilon).alpha(1.0).beta(0.0).build();
        } catch (Exception e) {
            logger.error(e.toString());
            dropTempSeries(new Path(tmp_series_name));
            return null;
        }
    }

    private void dropTempSeries(Path tmpPath) {
        String sql = String.format(DELETE_TEMP_SERIES_SQL, tmpPath);
        jdbcTemplate.execute(sql);
    }

    public List<SimilarityResult> queryDraw(List<Pair<Integer, Double>> query, Path path, Double epsilon) {
        KvMatchQueryRequest queryRequest = createTempSeries(path, query, epsilon);
        List<SimilarityResult> result =  getSimilarityResult(queryRequest);
        dropTempSeries(queryRequest.getQueryPath());
        return result;
    }
}