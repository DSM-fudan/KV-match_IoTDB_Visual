package cn.edu.fudan.dsm.controller;

import cn.edu.fudan.dsm.service.QueryService;
import cn.edu.thu.tsfile.common.utils.Pair;
import cn.edu.thu.tsfile.timeseries.read.qp.Path;
import cn.edu.thu.tsfiledb.index.kvmatch.KvMatchQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by dell on 2017/8/1.
 */
@Controller
public class QueryController {

    private static final Logger logger = LoggerFactory.getLogger(QueryController.class.getName());

    @Autowired
    private QueryService queryService;

    // 2017-08-02 16:25:51.567 2017-08-02 16:25:52.017
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    @ResponseBody
    public String queryGenerateOrOffset(String Q_query_Path, String startOffset, String endOffset, HttpServletRequest request) throws IOException, ParseException {
        // get parameters
        String path = (String) request.getSession().getAttribute("query-param_path");
        Double epsilon = (Double) request.getSession().getAttribute("query-param_epsilon");
        KvMatchQueryRequest queryRequest = KvMatchQueryRequest.builder(new Path(path), new Path(Q_query_Path),
                convertStringToLong(startOffset), convertStringToLong(endOffset), epsilon).alpha(1.0).beta(0.0).build();
        long clockStartTime = System.currentTimeMillis();
        Pair<Integer, List<Pair<Long, Double>>> result = queryService.query(queryRequest); // 起始时间，距离
        long clockEndTime = System.currentTimeMillis();
        logger.info("Answer: {}, Time usage: {} ms", result.left, clockEndTime - clockStartTime);
        if (!result.right.isEmpty()) {
            logger.info("Best: {}, Distance: {}", result.right.get(0).left, result.right.get(0).right);
        }

        String token = UUID.randomUUID().toString();
        request.getSession().setAttribute(token + "-path", path);
        request.getSession().setAttribute(token + "-Q_path", Q_query_Path);
        request.getSession().setAttribute(token + "-epsilon", epsilon);
        request.getSession().setAttribute(token + "-startOffset", convertStringToLong(startOffset));
        request.getSession().setAttribute(token + "-endOffset", convertStringToLong(endOffset));

        request.getSession().setAttribute(token + "-cntCandidates", result.left);
        request.getSession().setAttribute(token + "-answers", result.right);
        request.getSession().setAttribute(token + "-timeUsage", (clockEndTime - clockStartTime) / 1000.0);
        return token;
    }

    private long convertStringToLong(String offset) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(offset);
        return d.getTime();
    }

    @RequestMapping(value = "/queryDraw", method = RequestMethod.POST)
    @ResponseBody
    public String queryDraw(@RequestParam String queryStr, HttpServletRequest request) throws IOException {
        // get parameters
        String path = (String) request.getSession().getAttribute("query-path");
        Double epsilon = (Double) request.getSession().getAttribute("query-epsilon");

        // pre-process query series
        String[] querySplits = queryStr.split("\\|");
        List<Pair<Integer, Double>> queryTmp = new ArrayList<>();
        for (String querySplit : querySplits) {
            String[] pointSplit = querySplit.split(",");
            queryTmp.add(new Pair<>(Integer.parseInt(pointSplit[0]), Double.parseDouble(pointSplit[1])));
        }
        Integer length = queryTmp.get(queryTmp.size() - 1).left;
        List<Double> query = new ArrayList<>();
        query.add(queryTmp.get(0).right);
        for (int i = 1; i < queryTmp.size(); i++) {
            // amend points on the line
            double k = (queryTmp.get(i).right - queryTmp.get(i-1).right) / (queryTmp.get(i).left - queryTmp.get(i-1).left);
            for (long j = queryTmp.get(i-1).left + 1; j < queryTmp.get(i).left; j++) {
                query.add(queryTmp.get(i-1).right + (j - queryTmp.get(i-1).left) * k);
            }
            query.add(queryTmp.get(i).right);  // add current point
        }

//        queryService.setParameter(N, R, 1024, Wu, 6, true, length, epsilon);
        long clockStartTime = System.currentTimeMillis();

//        Pair<Integer, List<Pair<Long, Double>>> result = queryService.queryOnce(query);
        Pair<Integer, List<Pair<Long, Double>>> result = null;

        long clockEndTime = System.currentTimeMillis();

        logger.info("Candidates: {}, Answer: {}, Time usage: {} ms", result.left, result.right.size(), clockEndTime - clockStartTime);
        if (!result.right.isEmpty()) {
            logger.info("Best: {}, Distance: {}", result.right.get(0).left, result.right.get(0).right);
        }

        String token = UUID.randomUUID().toString();

        request.getSession().setAttribute(token + "-path", path);
        request.getSession().setAttribute(token + "-epsilon", epsilon);
        request.getSession().setAttribute(token + "-length", length);

        request.getSession().setAttribute(token + "-cntCandidates", result.left);
        request.getSession().setAttribute(token + "-answers", result.right);
        request.getSession().setAttribute(token + "-timeUsage", (clockEndTime - clockStartTime) / 1000.0);

        return token;
    }

}
