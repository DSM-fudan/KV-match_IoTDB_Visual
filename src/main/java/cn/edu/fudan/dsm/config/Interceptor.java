package cn.edu.fudan.dsm.config;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Jiaye Wu
 */
public class Interceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse res, Object handler, ModelAndView modelAndView) throws Exception {
        if (req.getSession().getAttribute("visited") == null) {
            setDefaultParameters(req);
        }
        if (req.getSession() != null && modelAndView != null) {
//                modelAndView.addObject("query_Wu", req.getSession().getAttribute("query-Wu"));
            modelAndView.addObject("param_path", req.getSession().getAttribute("query-param_path"));
            modelAndView.addObject("param_epsilon", req.getSession().getAttribute("query-param_epsilon"));
            modelAndView.addObject("param_normalized", req.getSession().getAttribute("query-param_normalized"));
            modelAndView.addObject("param_alpha", req.getSession().getAttribute("query-param_alpha"));
            modelAndView.addObject("param_beta", req.getSession().getAttribute("query-param_beta"));
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) throws Exception {

    }

    private void setDefaultParameters(HttpServletRequest request) {
        // set default parameters
        request.getSession().setAttribute("visited", true);
//            request.getSession().setAttribute("query-Wu", 50);
//        request.getSession().setAttribute("param_path","");
//        request.getSession().setAttribute("param-epsilon", 50.0);
        request.getSession().setAttribute("query-param_epsilon", 50.0);
        request.getSession().setAttribute("query-param_path", "");
        request.getSession().setAttribute("query-param_normalized", false);
        request.getSession().setAttribute("query-param_alpha", 1.2);
        request.getSession().setAttribute("query-param_beta", 5.0);
    }
}
