package cn.edu.fudan.dsm.controller;

import cn.edu.fudan.dsm.common.Series;
import cn.edu.fudan.dsm.common.TimeValue;
import cn.edu.fudan.dsm.service.QueryService;
import cn.edu.thu.tsfile.common.utils.Pair;
import cn.edu.thu.tsfile.timeseries.read.qp.Path;
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
 * Created by dell on 2017/8/1.
 */
@Controller
public class ResultController {

    private static final int MAX_RESULTS_DISPLAY = 100;

    @Autowired
    private QueryService queryService;

    @SuppressWarnings("unchecked")
    @RequestMapping("/result")
    public ModelAndView result(@RequestParam String token, @RequestParam(defaultValue = "0") Integer index, @RequestParam(defaultValue = "true") Boolean unique, HttpServletRequest request) throws IOException {
        // get stored query information
        List<String> param_paths = (List<String>) request.getSession().getAttribute("param_paths");
        String path = (String) request.getSession().getAttribute(token + "-path");
        String Q_path = (String) request.getSession().getAttribute(token + "-Q_path");
        Double epsilon = (Double) request.getSession().getAttribute(token + "-epsilon");
        Long startTime = (Long) request.getSession().getAttribute(token + "-startOffset");
        Long endTime = (Long) request.getSession().getAttribute(token + "-endOffset");
        Long length = endTime - startTime;

        List<Pair<Long, Double>> answers = (List<Pair<Long, Double>>) request.getSession().getAttribute(token + "-answers");
        Double timeUsage = (Double) request.getSession().getAttribute(token + "-timeUsage");

        ModelAndView mav = new ModelAndView("result");

        // get answer data series
        if (answers == null) {
            mav.setViewName("redirect:/");
            return mav;
        } else if (!answers.isEmpty()) {
            if (unique) {
                List<Pair<Long, Double>> uniqueAnswers = new ArrayList<>();
                int ansCnt = Math.min(answers.size(), MAX_RESULTS_DISPLAY);
                boolean[] visited = new boolean[ansCnt];
                for (int i = 0; i < ansCnt; i++) {
                    if (!visited[i]) {
                        uniqueAnswers.add(answers.get(i));
                        for (int j = i + 1; j < ansCnt; j++) {
                            if (!visited[j]) {
                                if (answers.get(j).left < answers.get(i).left + length && answers.get(j).left + length > answers.get(i).left) {
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
            List<TimeValue> data = queryService.getSeriesSimilar(new Path(path), answers.get(index).left, answers.get(index).left + length);
            mav.addObject("data", new Series(data));
            List<TimeValue> query = queryService.getSeriesSimilar(new Path(path), startTime, endTime);
            query = alignQandX(query, data);
            mav.addObject("query", new Series(query));
        } else {
            mav.addObject("cntAnswers", 0);
        }

        mav.addObject("param_paths", param_paths);
        mav.addObject("answers", answers);
        mav.addObject("timeUsage", timeUsage);
//        mav.addObject("indexTimeUsage", indexTimeUsage);

//        mav.addObject("Wu", Wu);
        mav.addObject("path", path);
        mav.addObject("Q_path", Q_path);
        mav.addObject("epsilon", epsilon);
        mav.addObject("length", length);

        mav.addObject("token", token);
        mav.addObject("index", index);
        mav.addObject("unique", unique);
        return mav;
    }

    // make query and data align in highcharts
    private List<TimeValue> alignQandX(List<TimeValue> query, List<TimeValue> data) {
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
