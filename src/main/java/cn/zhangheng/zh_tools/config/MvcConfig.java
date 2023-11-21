package cn.zhangheng.zh_tools.config;

import cn.zhangheng.zh_tools.config.interceptor.AndroidInter;
import cn.zhangheng.zh_tools.config.interceptor.BlackListInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-17 00:30
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Autowired
    private BlackListInterceptor blackListInterceptor;
    @Autowired
    private AndroidInter androidInter;
    @Value("#{'${my-filter0.exclude-path}'.split(',')}")
    private String[] black_list_exclude;
    public static String[] black_list_exclude1={"/favicon.ico", "/error",
            "/config/**",
            "/android_listener/**",
            "/static/**",
            "/666",
            "/proxy/**",
            "/getVerify/**",//验证码
            "/download/split/**"
    };

    private String[] android_add_path={"/config/**","/android_listener/**"};

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new LogInterceptor())
//        .excludePathPatterns("/favicon.ico", "/error");

        registry.addInterceptor(blackListInterceptor)
                .excludePathPatterns(black_list_exclude);

        registry.addInterceptor(androidInter)
                .addPathPatterns(android_add_path);
    }
}
