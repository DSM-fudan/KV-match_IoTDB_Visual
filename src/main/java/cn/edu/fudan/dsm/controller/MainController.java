package cn.edu.fudan.dsm.controller;

import cn.edu.fudan.dsm.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Ningting Pan
 */
@Controller
public class MainController {

    private final QueryService queryService;

    @Autowired
    public MainController(QueryService queryService) {
        this.queryService = queryService;
    }

    @RequestMapping("/")
    public ModelAndView index(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("index");
        List<String> param_paths = queryService.getPath();
        mav.addObject("param_paths", param_paths);
        request.getSession().setAttribute("param_paths", param_paths);
        return mav;
    }

    @RequestMapping("/about")
    public String about() {
        return "about";
    }

    @RequestMapping("/contact")
    public String contact() {
        return "contact";
    }

    @RequestMapping("/setParameter")
    @ResponseBody
    public String setParam(@RequestParam String param, @RequestParam String type, @RequestParam String value, HttpServletRequest request) {
        switch (type) {
            case "Long":
                request.getSession().setAttribute("query-" + param, Long.parseLong(value));
                break;
            case "Integer":
                request.getSession().setAttribute("query-" + param, Integer.parseInt(value));
                break;
            case "Double":
                request.getSession().setAttribute("query-" + param, Double.parseDouble(value));
                break;
            case "Boolean":
                request.getSession().setAttribute("query-" + param, Boolean.parseBoolean(value));
                break;
            default:
                request.getSession().setAttribute("query-" + param, value);
                break;
        }
        return "true";
    }
}
