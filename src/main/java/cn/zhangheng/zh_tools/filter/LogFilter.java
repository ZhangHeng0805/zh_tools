package cn.zhangheng.zh_tools.filter;

import com.zhangheng.util.CusAccessObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author: ZhangHeng
 * @email: zhangheng_0805@163.com
 * @date: 2023-07-10 15:03
 * @version: 1.0
 * @description: 打印日志
 */
@WebFilter
@Order(3)
public class LogFilter extends MyFilter {
    private Logger log= LoggerFactory.getLogger(getClass());
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String completeRequest = CusAccessObjectUtil.getCompleteRequest((HttpServletRequest) request);
        log.info("\n访问日志：{}",completeRequest);

//        log.info("访问用户：{}",mySession.getVisitors().size());
        chain.doFilter(request, response);
    }
}
