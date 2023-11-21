package cn.zhangheng.zh_tools.config.interceptor;

import com.zhangheng.util.CusAccessObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-17 00:31
 */

/**
 *
 */
public class LogInterceptor implements HandlerInterceptor {

    private Logger log = LoggerFactory.getLogger(this.getClass());



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String completeRequest = CusAccessObjectUtil.getCompleteRequest(request);
//        Map<String, Object> info = CusAccessObjectUtil.getRequestInfo(request);
//        request.getSession().setAttribute("request-info",info);
        log.info("\n访问日志:{}\n",completeRequest);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
