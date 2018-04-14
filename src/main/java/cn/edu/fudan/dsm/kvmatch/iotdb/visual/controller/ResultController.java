package cn.edu.fudan.dsm.kvmatch.iotdb.visual.controller;

import cn.edu.fudan.dsm.kvmatch.iotdb.visual.common.Series;
import cn.edu.fudan.dsm.kvmatch.iotdb.visual.common.SimilarityResult;
import cn.edu.fudan.dsm.kvmatch.iotdb.visual.common.TimeValue;
import cn.edu.fudan.dsm.kvmatch.iotdb.visual.service.QueryService;
import cn.edu.tsinghua.tsfile.timeseries.read.support.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ningting Pan
 */
@Controller
public class ResultController {

    private static final int MAX_RESULTS_DISPLAY = 100;

    private final QueryService queryService;

    @Autowired
    public ResultController(QueryService queryService) {
        this.queryService = queryService;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/result")
    public ModelAndView result(@RequestParam String token, @RequestParam(defaultValue = "0") Integer index, @RequestParam(defaultValue = "true") Boolean unique, HttpServletRequest request) throws IOException {
        // get stored query information
        List<String> param_paths = (List<String>) request.getSession().getAttribute("param_paths");
        String path = (String) request.getSession().getAttribute(token + "-path");
        Double epsilon = (Double) request.getSession().getAttribute(token + "-epsilon");
        Boolean normalized = (Boolean) request.getSession().getAttribute("query-param_normalized");
        Double alpha = (Double) request.getSession().getAttribute(token + "-alpha");
        Double beta = (Double) request.getSession().getAttribute(token + "-beta");

        // normal query request
        String Q_path = (String) request.getSession().getAttribute(token + "-Q_path");
        Long startTime = (Long) request.getSession().getAttribute(token + "-startOffset");
        Long endTime = (Long) request.getSession().getAttribute(token + "-endOffset");
        List<TimeValue> query;
        if (startTime != null && endTime != null) {
            // normal query request
            query = queryService.getSeriesSimilar(new Path(path), startTime, endTime);
        } else {
            // draw query request
            query = (List<TimeValue>) request.getSession().getAttribute(token + "-Q_query");
        }
        List<SimilarityResult> answers = (List<SimilarityResult>) request.getSession().getAttribute(token + "-answers");
        Double timeUsage = (Double) request.getSession().getAttribute(token + "-timeUsage");

        ModelAndView mav = new ModelAndView("result");

        // get answer data series
        if (answers == null) {
            mav.setViewName("redirect:/");
            return mav;
        } else if (!answers.isEmpty()) {
            if (unique) {
                List<SimilarityResult> uniqueAnswers = new ArrayList<>();
                int ansCnt = Math.min(answers.size(), MAX_RESULTS_DISPLAY);
                boolean[] visited = new boolean[ansCnt];
                for (int i = 0; i < ansCnt; i++) {
                    if (!visited[i]) {
                        uniqueAnswers.add(answers.get(i));
                        for (int j = i + 1; j < ansCnt; j++) {
                            if (!visited[j]) {
                                if (answers.get(j).getStartTime() < answers.get(i).getEndTime() && answers.get(j).getEndTime() > answers.get(i).getStartTime()) {
                                    visited[j] = true;
                                }
                            }
                        }
                    }
                }
                answers = uniqueAnswers;
                mav.addObject("cntAnswers", answers.size());
            } else {
                mav.addObject("cntAnswers", answers.size());
                if (answers.size() > MAX_RESULTS_DISPLAY) {
                    answers = answers.subList(0, MAX_RESULTS_DISPLAY);
                }
            }
            List<TimeValue> data = queryService.getSeriesSimilar(new Path(path), answers.get(index).getStartTime(), answers.get(index).getEndTime());
            mav.addObject("data", new Series(data));
            query = alignQAndX(query, data);

        } else {
            mav.addObject("cntAnswers", 0);
        }

        mav.addObject("param_paths", param_paths);
        mav.addObject("answers", answers);
        mav.addObject("timeUsage", timeUsage);
        mav.addObject("query", new Series(query));
        mav.addObject("path", path);
        mav.addObject("Q_path", Q_path);
        mav.addObject("param_epsilon", epsilon);
        mav.addObject("param_normalized", normalized);
        mav.addObject("param_alpha", alpha);
        mav.addObject("param_beta", beta);

        mav.addObject("token", token);
        mav.addObject("index", index);
        mav.addObject("unique", unique);
        return mav;
    }

    // make query and data align in Highcharts
    private List<TimeValue> alignQAndX(List<TimeValue> query, List<TimeValue> data) {
        if (query != null && !query.isEmpty() && data != null && !data.isEmpty()) {
            List<TimeValue> q = new ArrayList<>();
            long qt = query.get(0).getTime();
            long dt = data.get(0).getTime();
            long abs = qt - dt;
            for (TimeValue qtv : query) {
                qtv.setTime(qtv.getTime() - abs);
                q.add(qtv);
            }
            return q;
        } else {
            return query;
        }
    }
}
