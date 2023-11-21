package cn.zhangheng.zh_tools.config.interceptor;

import cn.zhangheng.zh_tools.bean.PhoneInfo;
import cn.zhangheng.zh_tools.service.entity_service.PhoneInfoService;
import com.zhangheng.util.CusAccessObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLDecoder;

/**
 * @author 张恒
 * @program: zh_tools
 * @email zhangheng.0805@qq.com
 * @date 2022-10-25 11:03
 */
@Configuration
public class AndroidInter implements HandlerInterceptor {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private PhoneInfoService phoneInfoService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*请求客户端校验
        * 若客户端请求头中的User-Agent格式错误，拦截401
        * */
        String requst = CusAccessObjectUtil.getRequst(request);
        try {
            PhoneInfo phoneInfo = phoneInfoService.saveAndflush(request);
            if (phoneInfo!=null) {
                HttpSession session = request.getSession();
                session.setAttribute("phone_info",phoneInfo);
                return true;
            }
        } catch (Exception e) {
        }
        String requestURI = request.getRequestURI();
        requestURI = URLDecoder.decode(requestURI, "UTF-8");
        log.warn(requestURI +":非法Android客户端：" + requst);
        response.sendError(401, "未授权请求");
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        /*请求打印日志*/
        int status = response.getStatus();
        String method = request.getMethod();
        String req = CusAccessObjectUtil.getRequst(request);
        String requestURI = request.getRequestURI();
        requestURI = URLDecoder.decode(requestURI, "UTF-8");
        log.info("\nandroid端(" + status + ")-["+method+"]" + requestURI + "：" + req+"\n");

    }
}
