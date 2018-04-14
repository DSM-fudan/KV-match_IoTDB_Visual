package cn.edu.fudan.dsm.kvmatch.iotdb.visual.controller;

import cn.edu.fudan.dsm.kvmatch.iotdb.visual.common.SimilarityResult;
import cn.edu.fudan.dsm.kvmatch.iotdb.visual.common.TimeValue;
import cn.edu.fudan.dsm.kvmatch.iotdb.visual.service.QueryService;
import cn.edu.tsinghua.iotdb.index.kvmatch.KvMatchQueryRequest;
import cn.edu.tsinghua.tsfile.common.utils.Pair;
import cn.edu.tsinghua.tsfile.timeseries.read.support.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Ningting Pan
 */
@Controller
public class QueryController {

    private static final Logger logger = LoggerFactory.getLogger(QueryController.class.getName());

    private final QueryService queryService;

    @Autowired
    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    @ResponseBody
    public String queryGenerateOrOffset(String Q_query_Path, String startOffset, String endOffset, HttpServletRequest request) {
        try {
            // get parameters
            String path = (String) request.getSession().getAttribute("query-param_path");
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("Which timeseries do you want to query? Please set it in the setting panel.");
            }
            Double epsilon = (Double) request.getSession().getAttribute("query-param_epsilon");
            Boolean normalized = (Boolean) request.getSession().getAttribute("query-param_normalized");
            Double alpha = (Double) request.getSession().getAttribute("query-param_alpha");
            Double beta = (Double) request.getSession().getAttribute("query-param_beta");
            if (!normalized || alpha == null || beta == null) {
                alpha = 1.0;
                beta = 0.0;
            }
            KvMatchQueryRequest queryRequest = KvMatchQueryRequest.builder(new Path(path), new Path(Q_query_Path),
                    convertStringToLong(startOffset), convertStringToLong(endOffset), epsilon).alpha(alpha).beta(beta).build();
            long clockStartTime = System.currentTimeMillis();
            List<SimilarityResult> result = queryService.query(queryRequest); // 起始时间，距离
            long clockEndTime = System.currentTimeMillis();
            logger.info("Answer: {}, Time usage: {} ms", result.size(), clockEndTime - clockStartTime);
            if (!result.isEmpty()) {
                logger.info("Best: start - {}, end - {}, Distance: {}", result.get(0).getStartTime(), result.get(0).getEndTime(), result.get(0).getDistance());
            }

            String token = UUID.randomUUID().toString();
            request.getSession().setAttribute(token + "-path", path);
            request.getSession().setAttribute(token + "-epsilon", epsilon);
            request.getSession().setAttribute(token + "-normalized", normalized);
            request.getSession().setAttribute(token + "-alpha", alpha);
            request.getSession().setAttribute(token + "-beta", beta);

            // normal query
            request.getSession().setAttribute(token + "-Q_path", Q_query_Path);
            long st = convertStringToLong(startOffset);
            long et = convertStringToLong(endOffset);
            request.getSession().setAttribute(token + "-startOffset", st);
            request.getSession().setAttribute(token + "-endOffset", et);

            // statistical information
            request.getSession().setAttribute(token + "-cntCandidates", result.size());
            request.getSession().setAttribute(token + "-answers", result);
            request.getSession().setAttribute(token + "-timeUsage", (clockEndTime - clockStartTime) / 1000.0);
            return token;
        } catch (Exception e) {
            return "<strong>Error</strong> " + e.getMessage();
        }
    }

    private long convertStringToLong(String offset) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(offset);
        return d.getTime();
    }

    @RequestMapping(value = "/queryDraw", method = RequestMethod.POST)
    @ResponseBody
    public String queryDraw(@RequestParam String queryStr, HttpServletRequest request) {
        try {
            // get parameters
            String path = (String) request.getSession().getAttribute("query-param_path");
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("Which timeseries do you want to query? Please set it in the setting panel.");
            }
            Double epsilon = (Double) request.getSession().getAttribute("query-param_epsilon");
            Boolean normalized = (Boolean) request.getSession().getAttribute("query-param_normalized");
            Double alpha = (Double) request.getSession().getAttribute("query-param_alpha");
            Double beta = (Double) request.getSession().getAttribute("query-param_beta");
            if (!normalized || alpha == null || beta == null) {
                alpha = 1.0;
                beta = 0.0;
            }
            // pre-process query series
            String[] querySplits = queryStr.split("\\|");
            List<Pair<Integer, Double>> query = new ArrayList<>();
            for (String querySplit : querySplits) {
                String[] pointSplit = querySplit.split(",");
                query.add(new Pair<>(Integer.parseInt(pointSplit[0]), Double.parseDouble(pointSplit[1])));
            }
            long clockStartTime = System.currentTimeMillis();
            List<SimilarityResult> result = queryService.queryDraw(query, new Path(path), epsilon, alpha, beta);
            long clockEndTime = System.currentTimeMillis();
            if (result == null) {
                logger.error("Query failed.");
                return "";
            }
            logger.info("Answer: {}, Time usage: {} ms", result.size(), clockEndTime - clockStartTime);
            if (!result.isEmpty()) {
                logger.info("Best: start - {}, end - {}, Distance: {}", result.get(0).getStartTime(), result.get(0).getEndTime(), result.get(0).getDistance());
            }

            String token = UUID.randomUUID().toString();

            request.getSession().setAttribute(token + "-path", path);
            request.getSession().setAttribute(token + "-epsilon", epsilon);
            request.getSession().setAttribute(token + "-normalized", normalized);
            request.getSession().setAttribute(token + "-alpha", alpha);
            request.getSession().setAttribute(token + "-beta", beta);

            // draw query
            List<TimeValue> Q_query = new ArrayList<>();
            for (Pair<Integer, Double> q : query) {
                Q_query.add(new TimeValue((long) q.left, q.right));
            }
            request.getSession().setAttribute(token + "-Q_query", Q_query);

            request.getSession().setAttribute(token + "-cntAnswers", result.size());
            request.getSession().setAttribute(token + "-answers", result);
            request.getSession().setAttribute(token + "-timeUsage", (clockEndTime - clockStartTime) / 1000.0);

            return token;
        } catch (Exception e) {
            return "<strong>Error</strong> " + e.getMessage();
        }
    }

    @RequestMapping(value = "createIndex", method = RequestMethod.POST)
    @ResponseBody
    public String createIndex(String index_path) {
        try {
            queryService.createIndex(index_path);
            return "<strong>Success</strong> " + "Index created on timeseries " + index_path + ".";
        } catch (Exception e) {
            if (e.toString().contains("has already been indexed")) {
                logger.warn(e.getMessage(), e.getCause());
                return "<strong>Warning</strong> Timeseries " + index_path + " has already been indexed.";
            } else {
                logger.error(e.getMessage(), e.getCause());
                return "<strong>Error</strong> Timeseries " + index_path + " can't be indexed.";
            }
        }
    }
}
