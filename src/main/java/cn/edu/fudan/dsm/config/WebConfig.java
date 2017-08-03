package cn.edu.fudan.dsm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by dell on 2017/8/1.
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(new Interceptor()).addPathPatterns("/**");
    }

}
